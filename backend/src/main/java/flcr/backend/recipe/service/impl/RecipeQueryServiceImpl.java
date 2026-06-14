package flcr.backend.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.community.entity.Collection;
import flcr.backend.community.entity.Like;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.LikeMapper;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.recipe.service.RecipeQueryService;
import flcr.backend.recipe.util.DifficultyUtil;
import flcr.backend.recipe.util.RecipeDtoAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeQueryServiceImpl implements RecipeQueryService {

    private final RecipeMapper recipeMapper;
    private final LikeMapper likeMapper;
    private final CollectionMapper collectionMapper;
    private final RecipeDtoAssembler recipeDtoAssembler;

    @Override
    public Page<RecipeListItemResponseDTO> getRecipeList(RecipeListRequestDTO request) {
        Page<Recipe> recipePage = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();

        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            wrapper.eq(Recipe::getCategory, request.getCategory());
        }
        if (request.getDifficulty() != null && !request.getDifficulty().isEmpty()) {
            wrapper.eq(Recipe::getDifficulty, DifficultyUtil.convertDifficulty(request.getDifficulty()));
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(Recipe::getName, request.getKeyword());
        }

        wrapper.orderByDesc(Recipe::getCreatedAt);
        Page<Recipe> result = recipeMapper.selectPage(recipePage, wrapper);

        List<RecipeListItemResponseDTO> dtoList = result.getRecords().stream()
                .map(recipeDtoAssembler::convertToListItemDTO)
                .collect(Collectors.toList());

        Page<RecipeListItemResponseDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public RecipeDetailResponseDTO getRecipeDetail(Long recipeId) {
        Long userId = UserContext.getUserId();
        Recipe recipe = recipeMapper.selectById(recipeId);
        if (recipe == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "菜谱不存在");
        }

        LambdaUpdateWrapper<Recipe> viewWrapper = new LambdaUpdateWrapper<>();
        viewWrapper.eq(Recipe::getId, recipeId)
                .setSql("view_count = view_count + 1");
        recipeMapper.update(null, viewWrapper);
        recipe.setViewCount(recipe.getViewCount() + 1);

        RecipeDetailResponseDTO dto = recipeDtoAssembler.convertToDetailDTO(recipe);

        if (userId != null) {
            dto.setIsLiked(checkLiked(userId, recipeId, 1));
            dto.setIsCollected(checkCollected(userId, recipeId));
        } else {
            dto.setIsLiked(false);
            dto.setIsCollected(false);
        }

        return dto;
    }

    private boolean checkLiked(Long userId, Long targetId, Integer targetType) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetId, targetId)
                .eq(Like::getTargetType, targetType);
        return likeMapper.selectCount(wrapper) > 0;
    }

    private boolean checkCollected(Long userId, Long recipeId) {
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getUserId, userId)
                .eq(Collection::getRecipeId, recipeId);
        return collectionMapper.selectCount(wrapper) > 0;
    }
}
