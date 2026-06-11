package flcr.backend.ingredient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.ingredient.DTO.request.IngredientAddRequestDTO;
import flcr.backend.ingredient.cache.CachedIngredient;
import flcr.backend.ingredient.cache.IngredientHeapCache;
import flcr.backend.ingredient.constants.IngredientStatus;
import flcr.backend.ingredient.DTO.request.IngredientBatchAddRequestDTO;
import flcr.backend.ingredient.DTO.request.IngredientListRequestDTO;
import flcr.backend.ingredient.DTO.request.IngredientUpdateRequestDTO;
import flcr.backend.ingredient.DTO.response.CommonIngredientResponseDTO;
import flcr.backend.ingredient.DTO.response.ExpiringNoticeResponseDTO;
import flcr.backend.ingredient.DTO.response.IngredientListResponseDTO;
import flcr.backend.ingredient.DTO.response.IngredientResponseDTO;
import flcr.backend.ingredient.entity.CommonIngredient;
import flcr.backend.ingredient.entity.Ingredient;
import flcr.backend.ingredient.mapper.CommonIngredientMapper;
import flcr.backend.ingredient.mapper.IngredientMapper;
import flcr.backend.ingredient.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {

    private final IngredientMapper ingredientMapper;
    private final CommonIngredientMapper commonIngredientMapper;
    private final IngredientHeapCache heapCache;

    @Override
    public IngredientListResponseDTO list(IngredientListRequestDTO query) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Ingredient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ingredient::getUserId, userId);

        if (query.getCategory() != null && !query.getCategory().isEmpty()) {
            wrapper.eq(Ingredient::getCategory, query.getCategory());
        }

        if ("expireDate".equals(query.getSortBy())) {
            if ("asc".equals(query.getSort())) {
                wrapper.orderByAsc(Ingredient::getExpireDate);
            } else {
                wrapper.orderByDesc(Ingredient::getExpireDate);
            }
        } else {
            if ("asc".equals(query.getSort())) {
                wrapper.orderByAsc(Ingredient::getCreatedAt);
            } else {
                wrapper.orderByDesc(Ingredient::getCreatedAt);
            }
        }

        List<Ingredient> ingredients = ingredientMapper.selectList(wrapper);

        if (query.getStatus() != null) {
            ingredients = ingredients.stream()
                    .filter(i -> query.getStatus().equals(IngredientStatus.compute(i.getExpireDate()).getCode()))
                    .collect(Collectors.toList());
        }

        List<IngredientResponseDTO> dtoList = ingredients.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        int expiredCount = (int) ingredients.stream()
                .filter(i -> IngredientStatus.compute(i.getExpireDate()).isExpired())
                .count();
        int urgentCount = (int) ingredients.stream()
                .filter(i -> IngredientStatus.compute(i.getExpireDate()).isUrgent())
                .count();
        int warningCount = (int) ingredients.stream()
                .filter(i -> IngredientStatus.compute(i.getExpireDate()).isWarning())
                .count();
        int normalCount = (int) ingredients.stream()
                .filter(i -> IngredientStatus.compute(i.getExpireDate()).isNormal())
                .count();

        return IngredientListResponseDTO.builder()
                .ingredients(dtoList)
                .summary(IngredientListResponseDTO.Summary.builder()
                        .totalCount(dtoList.size())
                        .expiredCount(expiredCount)
                        .urgentCount(urgentCount)
                        .warningCount(warningCount)
                        .normalCount(normalCount)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public Long add(IngredientAddRequestDTO request) {
        Long userId = UserContext.getUserId();
        Ingredient ingredient = new Ingredient();
        ingredient.setUserId(userId);
        ingredient.setName(request.getName());
        ingredient.setQuantity(request.getQuantity());
        ingredient.setUnit(request.getUnit());
        ingredient.setCategory(request.getCategory());
        ingredient.setStorageCondition(request.getStorageCondition());
        ingredient.setExpireDate(request.getExpireDate());
        ingredient.setCreatedAt(LocalDateTime.now());
        ingredient.setUpdatedAt(LocalDateTime.now());

        ingredientMapper.insert(ingredient);

        if (ingredient.getExpireDate() != null) {
            IngredientStatus status = IngredientStatus.compute(ingredient.getExpireDate());
            if (!status.isNormal()) {
                heapCache.push(CachedIngredient.from(ingredient, status.getCode()));
            }
        }

        return ingredient.getId();
    }

    @Override
    @Transactional
    public void update(Long id, IngredientUpdateRequestDTO request) {
        Long userId = UserContext.getUserId();
        Ingredient ingredient = ingredientMapper.selectById(id);
        if (ingredient == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "食材不存在");
        }
        if (!ingredient.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_ERROR, "无权修改该食材");
        }

        if (request.getName() != null) {
            ingredient.setName(request.getName());
        }
        if (request.getQuantity() != null) {
            ingredient.setQuantity(request.getQuantity());
        }
        if (request.getUnit() != null) {
            ingredient.setUnit(request.getUnit());
        }
        if (request.getCategory() != null) {
            ingredient.setCategory(request.getCategory());
        }
        if (request.getExpireDate() != null) {
            ingredient.setExpireDate(request.getExpireDate());
            ingredient.setReaded(false);
        }
        if (request.getStorageCondition() != null) {
            ingredient.setStorageCondition(request.getStorageCondition());
        }
        ingredient.setUpdatedAt(LocalDateTime.now());

        ingredientMapper.updateById(ingredient);

        if (request.getExpireDate() != null) {
            heapCache.remove(id, userId);
            IngredientStatus newStatus = IngredientStatus.compute(ingredient.getExpireDate());
            if (!newStatus.isNormal()) {
                heapCache.push(CachedIngredient.from(ingredient, newStatus.getCode()));
            }
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        Ingredient ingredient = ingredientMapper.selectById(id);
        if (ingredient == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "食材不存在");
        }
        if (!ingredient.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_ERROR, "无权删除该食材");
        }

        ingredientMapper.deleteById(id);
        heapCache.remove(id, userId);
    }

    @Override
    @Transactional
    public void markRead(Long id) {
        Long userId = UserContext.getUserId();
        Ingredient ingredient = ingredientMapper.selectById(id);
        if (ingredient == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "食材不存在");
        }
        if (!ingredient.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_ERROR, "无权修改该食材");
        }
        ingredient.setReaded(true);
        ingredient.setUpdatedAt(LocalDateTime.now());
        ingredientMapper.updateById(ingredient);
        heapCache.markRead(id, userId, true);
    }

    @Override
    @Transactional
    public void markBatchRead(List<Long> ids) {
        Long userId = UserContext.getUserId();
        for (Long id : ids) {
            Ingredient ingredient = ingredientMapper.selectById(id);
            if (ingredient == null) {
                throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "食材不存在: " + id);
            }
            if (!ingredient.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.PERMISSION_ERROR, "无权修改该食材: " + id);
            }
            ingredient.setReaded(true);
            ingredient.setUpdatedAt(LocalDateTime.now());
            ingredientMapper.updateById(ingredient);
            heapCache.markRead(id, userId, true);
        }
    }

    @Override
    @Transactional
    public List<Long> batchAdd(IngredientBatchAddRequestDTO request) {
        Long userId = UserContext.getUserId();
        List<Long> ids = new ArrayList<>();

        for (IngredientBatchAddRequestDTO.BatchItem item : request.getIngredients()) {
            Ingredient ingredient = new Ingredient();
            ingredient.setUserId(userId);
            ingredient.setName(item.getName());
            ingredient.setQuantity(item.getQuantity());
            ingredient.setUnit(item.getUnit());
            ingredient.setCategory(item.getCategory());
            ingredient.setExpireDate(item.getExpireDate());
            ingredient.setCreatedAt(LocalDateTime.now());
            ingredient.setUpdatedAt(LocalDateTime.now());

            ingredientMapper.insert(ingredient);
            ids.add(ingredient.getId());
        }

        return ids;
    }

    @Override
    public ExpiringNoticeResponseDTO expiringNotice() {
        Long userId = UserContext.getUserId();
        LocalDate today = LocalDate.now();

        List<ExpiringNoticeResponseDTO.Item> items = new ArrayList<>();
        boolean hasUnread = false;

        List<CachedIngredient> expired = heapCache.peekExpired(Integer.MAX_VALUE);
        for (CachedIngredient ingredient : expired) {
            if (!ingredient.getUserId().equals(userId)) continue;
            long daysLeft = ChronoUnit.DAYS.between(today, ingredient.getExpireDate());
            items.add(ExpiringNoticeResponseDTO.Item.builder()
                    .id(ingredient.getId())
                    .userId(ingredient.getUserId())
                    .name(ingredient.getName())
                    .expireDate(ingredient.getExpireDate())
                    .daysLeft(daysLeft)
                    .status(IngredientStatus.EXPIRED.getCode())
                    .build());
            if (ingredient.getReaded() == null || !ingredient.getReaded()) {
                hasUnread = true;
            }
        }

        List<CachedIngredient> urgent = heapCache.peekUrgent(Integer.MAX_VALUE);
        for (CachedIngredient ingredient : urgent) {
            if (!ingredient.getUserId().equals(userId)) continue;
            long daysLeft = ChronoUnit.DAYS.between(today, ingredient.getExpireDate());
            items.add(ExpiringNoticeResponseDTO.Item.builder()
                    .id(ingredient.getId())
                    .userId(ingredient.getUserId())
                    .name(ingredient.getName())
                    .expireDate(ingredient.getExpireDate())
                    .daysLeft(daysLeft)
                    .status(IngredientStatus.URGENT.getCode())
                    .build());
            if (ingredient.getReaded() == null || !ingredient.getReaded()) {
                hasUnread = true;
            }
        }

        List<CachedIngredient> warning = heapCache.peekWarning(Integer.MAX_VALUE);
        for (CachedIngredient ingredient : warning) {
            if (!ingredient.getUserId().equals(userId)) continue;
            long daysLeft = ChronoUnit.DAYS.between(today, ingredient.getExpireDate());
            items.add(ExpiringNoticeResponseDTO.Item.builder()
                    .id(ingredient.getId())
                    .userId(ingredient.getUserId())
                    .name(ingredient.getName())
                    .expireDate(ingredient.getExpireDate())
                    .daysLeft(daysLeft)
                    .status(IngredientStatus.WARNING.getCode())
                    .build());
            if (ingredient.getReaded() == null || !ingredient.getReaded()) {
                hasUnread = true;
            }
        }

        return ExpiringNoticeResponseDTO.builder()
                .items(items)
                .summary(ExpiringNoticeResponseDTO.Summary.builder()
                        .hasnUnread(hasUnread)
                        .build())
                .build();
    }

    @Override
    public CommonIngredientResponseDTO commonList() {
        List<CommonIngredient> all = commonIngredientMapper.selectList(null);

        Map<String, List<CommonIngredientResponseDTO.Item>> grouped = new LinkedHashMap<>();
        for (CommonIngredient ci : all) {
            grouped.computeIfAbsent(ci.getCategory(), k -> new ArrayList<>())
                    .add(CommonIngredientResponseDTO.Item.builder()
                            .name(ci.getName())
                            .defaultUnit(ci.getDefaultUnit())
                            .build());
        }

        List<CommonIngredientResponseDTO.CategoryGroup> categories = grouped.entrySet().stream()
                .map(entry -> CommonIngredientResponseDTO.CategoryGroup.builder()
                        .name(entry.getKey())
                        .items(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return CommonIngredientResponseDTO.builder()
                .categories(categories)
                .build();
    }

    private IngredientResponseDTO toResponseDTO(Ingredient ingredient) {
        Integer status = IngredientStatus.compute(ingredient.getExpireDate()).getCode();
        Long daysLeft = null;
        if (ingredient.getExpireDate() != null) {
            daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), ingredient.getExpireDate());
        }

        return IngredientResponseDTO.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .quantity(ingredient.getQuantity())
                .unit(ingredient.getUnit())
                .category(ingredient.getCategory())
                .storageCondition(ingredient.getStorageCondition())
                .expireDate(ingredient.getExpireDate())
                .daysLeft(daysLeft)
                .status(status)
                .readed(ingredient.getReaded())
                .createdAt(ingredient.getCreatedAt())
                .build();
    }
}
