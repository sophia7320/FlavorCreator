package flcr.backend.ingredient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.ingredient.DTO.request.IngredientAddRequestDTO;
import flcr.backend.ingredient.DTO.request.IngredientBatchAddRequestDTO;
import flcr.backend.ingredient.DTO.request.IngredientListQueryDTO;
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

    @Override
    public IngredientListResponseDTO list(IngredientListQueryDTO query) {
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

        if (query.getStatus() != null && !query.getStatus().isEmpty() && !"all".equals(query.getStatus())) {
            ingredients = ingredients.stream()
                    .filter(i -> query.getStatus().equals(computeStatus(i.getExpireDate())))
                    .collect(Collectors.toList());
        }

        List<IngredientResponseDTO> dtoList = ingredients.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        long expiringCount = ingredients.stream()
                .filter(i -> "expiring".equals(computeStatus(i.getExpireDate())))
                .count();
        long expiredCount = ingredients.stream()
                .filter(i -> "expired".equals(computeStatus(i.getExpireDate())))
                .count();

        return IngredientListResponseDTO.builder()
                .ingredients(dtoList)
                .summary(IngredientListResponseDTO.Summary.builder()
                        .totalCount(dtoList.size())
                        .expiringCount((int) expiringCount)
                        .expiredCount((int) expiredCount)
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
        }
        if (request.getStorageCondition() != null) {
            ingredient.setStorageCondition(request.getStorageCondition());
        }
        ingredient.setUpdatedAt(LocalDateTime.now());

        ingredientMapper.updateById(ingredient);
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
        LambdaQueryWrapper<Ingredient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ingredient::getUserId, userId)
                .isNotNull(Ingredient::getExpireDate);

        List<Ingredient> ingredients = ingredientMapper.selectList(wrapper);
        LocalDate today = LocalDate.now();

        List<ExpiringNoticeResponseDTO.ExpiringItem> expiring = new ArrayList<>();
        List<ExpiringNoticeResponseDTO.ExpiringItem> expired = new ArrayList<>();

        for (Ingredient ingredient : ingredients) {
            String status = computeStatus(ingredient.getExpireDate());
            long daysLeft = ChronoUnit.DAYS.between(today, ingredient.getExpireDate());

            ExpiringNoticeResponseDTO.ExpiringItem item = ExpiringNoticeResponseDTO.ExpiringItem.builder()
                    .id(ingredient.getId())
                    .name(ingredient.getName())
                    .expireDate(ingredient.getExpireDate())
                    .daysLeft(daysLeft)
                    .build();

            if ("expired".equals(status)) {
                expired.add(item);
            } else if ("expiring".equals(status)) {
                expiring.add(item);
            }
        }

        return ExpiringNoticeResponseDTO.builder()
                .expiring(expiring)
                .expired(expired)
                .summary(ExpiringNoticeResponseDTO.Summary.builder()
                        .expiringCount(expiring.size())
                        .expiredCount(expired.size())
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
        String status = computeStatus(ingredient.getExpireDate());
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
                .createdAt(ingredient.getCreatedAt())
                .build();
    }

    private String computeStatus(LocalDate expireDate) {
        if (expireDate == null) {
            return "normal";
        }
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expireDate);
        if (daysLeft < 0) {
            return "expired";
        }
        if (daysLeft <= 3) {
            return "expiring";
        }
        return "normal";
    }
}
