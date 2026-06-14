package flcr.backend.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.constants.SourceConstants;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.community.entity.Collection;
import flcr.backend.community.entity.Comment;
import flcr.backend.community.entity.Like;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.CommentMapper;
import flcr.backend.community.mapper.LikeMapper;
import flcr.backend.recipe.DTO.request.CreateRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeUpdateRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.recipe.service.RecipeWriteService;
import flcr.backend.recipe.util.RecipeDtoAssembler;
import flcr.backend.recipe.util.RecipeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecipeWriteServiceImpl implements RecipeWriteService {

    private final RecipeMapper recipeMapper;
    private final RecipeValidator recipeValidator;
    private final RecipeDtoAssembler recipeDtoAssembler;
    private final LikeMapper likeMapper;
    private final CollectionMapper collectionMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public Long publishRecipe(CreateRecipeRequestDTO request) {
        Long userId = UserContext.getUserId();
        Recipe recipe = buildRecipe(request, userId);
        recipeMapper.insert(recipe);
        return recipe.getId();
    }

    @Override
    @Transactional
    public RecipeDetailResponseDTO updateRecipe(Long recipeId, RecipeUpdateRequestDTO request) {
        Long userId = UserContext.getUserId();
        Recipe recipe = recipeMapper.selectById(recipeId);
        if (recipe == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "菜谱不存在");
        }
        if (!recipe.getAuthorId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_ERROR, "无权修改该菜谱");
        }

        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            recipeValidator.validateCategory(request.getCategory());
        }
        if (request.getIngredients() != null && !request.getIngredients().isBlank()) {
            recipeValidator.validateJsonField(request.getIngredients(), "ingredients");
        }
        if (request.getSteps() != null && !request.getSteps().isBlank()) {
            recipeValidator.validateJsonField(request.getSteps(), "steps");
        }
        if (request.getTags() != null && !request.getTags().isBlank()) {
            recipeValidator.validateJsonField(request.getTags(), "tags");
        }

        if (request.getName() != null) recipe.setName(request.getName());
        if (request.getCoverUrl() != null) recipe.setCover(request.getCoverUrl());
        if (request.getDesc() != null) recipe.setDesc(request.getDesc());
        if (request.getCategory() != null) recipe.setCategory(request.getCategory());
        if (request.getTips() != null) recipe.setTips(request.getTips());
        if (request.getCookTime() != null) recipe.setCookTime(request.getCookTime());
        if (request.getDifficulty() != null) recipe.setDifficulty(request.getDifficulty());
        if (request.getCalories() != null) recipe.setCalories(request.getCalories());
        if (request.getImageUrls() != null) {
            recipe.setImages(request.getImageUrls());
        }
        if (request.getIngredients() != null) recipe.setIngredients(request.getIngredients());
        if (request.getSteps() != null) recipe.setSteps(request.getSteps());
        if (request.getTags() != null) recipe.setTags(request.getTags());

        recipe.setUpdatedAt(LocalDateTime.now());
        recipeMapper.updateById(recipe);

        return recipeDtoAssembler.convertToDetailDTO(recipe);
    }

    @Override
    @Transactional
    public void deleteRecipe(Long recipeId) {
        Long userId = UserContext.getUserId();
        Recipe recipe = recipeMapper.selectById(recipeId);
        if (recipe == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "菜谱不存在");
        }
        if (!recipe.getAuthorId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_ERROR, "无权删除该菜谱");
        }

        LambdaQueryWrapper<Like> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(Like::getTargetId, recipeId)
                .eq(Like::getTargetType, 1);
        likeMapper.delete(likeWrapper);

        LambdaQueryWrapper<Collection> collectionWrapper = new LambdaQueryWrapper<>();
        collectionWrapper.eq(Collection::getRecipeId, recipeId);
        collectionMapper.delete(collectionWrapper);

        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getRecipeId, recipeId);
        commentMapper.delete(commentWrapper);

        recipeMapper.deleteById(recipeId);
    }

    private Recipe buildRecipe(CreateRecipeRequestDTO request, Long userId) {
        Recipe recipe = new Recipe();
        applyDtoToRecipe(request, recipe);
        recipe.setAuthorId(userId);
        recipe.setSource(SourceConstants.USER);
        recipe.setLikeCount(0);
        recipe.setCollectionCount(0);
        recipe.setCommentCount(0);
        recipe.setViewCount(0);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());
        return recipe;
    }

    private void applyDtoToRecipe(CreateRecipeRequestDTO dto, Recipe recipe) {
        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            recipeValidator.validateCategory(dto.getCategory());
        }
        recipeValidator.validateJsonField(dto.getIngredients(), "ingredients");
        recipeValidator.validateJsonField(dto.getSteps(), "steps");
        if (dto.getTags() != null && !dto.getTags().isBlank()) {
            recipeValidator.validateJsonField(dto.getTags(), "tags");
        }

        recipe.setName(dto.getName());
        recipe.setCover(dto.getCoverUrl());
        recipe.setDesc(dto.getDesc());
        recipe.setCategory(dto.getCategory());
        recipe.setTips(dto.getTips());
        recipe.setCookTime(dto.getCookTime());
        recipe.setDifficulty(dto.getDifficulty());
        recipe.setCalories(dto.getCalories());
        recipe.setImages(dto.getImageUrls());
        recipe.setIngredients(dto.getIngredients());
        recipe.setSteps(dto.getSteps());
        recipe.setTags(dto.getTags());
    }
}
