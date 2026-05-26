package flcr.backend.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.constants.SourceConstants;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.service.FileStorageService;
import flcr.backend.common.service.ImageModerationService;
import flcr.backend.community.entity.Collection;
import flcr.backend.community.entity.Like;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.LikeMapper;
import flcr.backend.recipe.DTO.request.PublishRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemDTO;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeMapper recipeMapper;
    private final LikeMapper likeMapper;
    private final CollectionMapper collectionMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final FileStorageService fileStorageService;
    private final ImageModerationService imageModerationService;

    @Override
    @Transactional
    public Long publishRecipe(PublishRecipeRequestDTO request, MultipartFile cover,
                              List<MultipartFile> images) {
        Long userId = UserContext.getUserId();

        // Phase 1: Validate all files (format + size, no side effects)
        if (cover != null) {
            imageModerationService.validate(cover, "recipe-cover");
        }
        if (images != null) {
            for (MultipartFile image : images) {
                imageModerationService.validate(image, "recipe-image");
            }
        }

        // Phase 2: Store cover
        String coverUrl = "";
        List<String> storedUrls = new ArrayList<>();
        try {
            if (cover != null) {
                coverUrl = fileStorageService.store(cover, "recipe-cover");
                storedUrls.add(coverUrl);
            }

            // Phase 3: Store all images
            List<String> imageUrls = new ArrayList<>();
            if (images != null) {
                for (MultipartFile image : images) {
                    String imageUrl = fileStorageService.store(image, "recipe-image");
                    imageUrls.add(imageUrl);
                    storedUrls.add(imageUrl);
                }
            }

            // Phase 4: Moderate all (content check, all must pass)
            if (cover != null) {
                imageModerationService.moderate(coverUrl, "recipe-cover");
            }
            for (String imageUrl : imageUrls) {
                imageModerationService.moderate(imageUrl, "recipe-image");
            }

            // Phase 5: Build and save recipe (only if all checks pass)
            Recipe recipe = buildRecipe(request, coverUrl, imageUrls, userId);
            recipeMapper.insert(recipe);
            storedUrls.clear(); // success, don't clean up
            return recipe.getId();

        } finally {
            // Compensation: if anything went wrong, clean up stored files
            for (String url : storedUrls) {
                fileStorageService.delete(url);
            }
        }
    }

    private Recipe buildRecipe(PublishRecipeRequestDTO request, String coverUrl,
                                List<String> imageUrls, Long userId) {
        Recipe recipe = new Recipe();
        recipe.setName(request.getName());
        recipe.setCover(coverUrl);
        try {
            recipe.setImages(objectMapper.writeValueAsString(imageUrls));
            recipe.setIngredients(request.getIngredients());
            recipe.setSteps(request.getSteps());
            recipe.setTags(request.getTags());
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "JSON处理失败");
        }
        recipe.setAuthorId(userId);
        recipe.setCategory(request.getCategory());
        recipe.setTips(request.getTips());
        recipe.setCookTime(request.getCookTime());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setCalories(request.getCalories());
        recipe.setSource(SourceConstants.USER);
        recipe.setLikeCount(0);
        recipe.setCollectionCount(0);
        recipe.setCommentCount(0);
        recipe.setViewCount(0);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());
        return recipe;
    }

    @Override
    public Page<RecipeListItemDTO> getRecipeList(RecipeListRequestDTO request) {
        Page<Recipe> recipePage = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();

        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            wrapper.eq(Recipe::getCategory, request.getCategory());
        }
        if (request.getDifficulty() != null && !request.getDifficulty().isEmpty()) {
            wrapper.eq(Recipe::getDifficulty, convertDifficulty(request.getDifficulty()));
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(Recipe::getName, request.getKeyword());
        }

        wrapper.orderByDesc(Recipe::getCreatedAt);
        Page<Recipe> result = recipeMapper.selectPage(recipePage, wrapper);

        List<RecipeListItemDTO> dtoList = result.getRecords().stream()
                .map(this::convertToListItemDTO)
                .collect(Collectors.toList());

        Page<RecipeListItemDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public RecipeDetailDTO getRecipeDetail(Long recipeId) {
        Long userId = UserContext.getUserId();
        Recipe recipe = recipeMapper.selectById(recipeId);
        if (recipe == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "菜谱不存在");
        }

        LambdaUpdateWrapper<Recipe> viewWrapper = new LambdaUpdateWrapper<>();
        viewWrapper.eq(Recipe::getId, recipeId)
                .setSql("view_count = view_count + 1");
        recipeMapper.update(null, viewWrapper);

        RecipeDetailDTO dto = convertToDetailDTO(recipe);

        if (userId != null) {
            dto.setIsLiked(checkLiked(userId, recipeId, 1));
            dto.setIsCollected(checkCollected(userId, recipeId));
        } else {
            dto.setIsLiked(false);
            dto.setIsCollected(false);
        }

        return dto;
    }

    // ==================== 私有辅助方法 ====================

    private RecipeListItemDTO convertToListItemDTO(Recipe recipe) {
        User author = userMapper.selectById(recipe.getAuthorId());

        String[] tags = {};
        try {
            if (recipe.getTags() != null) {
                tags = objectMapper.readValue(recipe.getTags(), String[].class);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "JSON解析失败");
        }

        return RecipeListItemDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .cover(recipe.getCover())
                .author(RecipeListItemDTO.AuthorInfo.builder()
                        .id(author != null ? (long) author.getId() : null)
                        .nickname(author != null ? author.getNickname() : "未知用户")
                        .avatar(author != null ? author.getAvatar() : "")
                        .build())
                .cookTime(recipe.getCookTime())
                .difficulty(convertDifficultyToString(recipe.getDifficulty()))
                .calories(recipe.getCalories())
                .tags(tags)
                .stats(RecipeListItemDTO.RecipeStats.builder()
                        .likes(recipe.getLikeCount())
                        .collections(recipe.getCollectionCount())
                        .comments(recipe.getCommentCount())
                        .views(recipe.getViewCount())
                        .build())
                .createdAt(recipe.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    private RecipeDetailDTO convertToDetailDTO(Recipe recipe) {
        User author = userMapper.selectById(recipe.getAuthorId());

        List<String> images = new ArrayList<>();
        try {
            if (recipe.getImages() != null) {
                images = objectMapper.readValue(recipe.getImages(), new TypeReference<List<String>>() {});
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "JSON解析失败");
        }

        List<RecipeDetailDTO.IngredientItem> ingredients = new ArrayList<>();
        List<RecipeDetailDTO.StepItem> steps = new ArrayList<>();
        String[] tags = {};

        try {
            if (recipe.getIngredients() != null) {
                ingredients = objectMapper.readValue(recipe.getIngredients(),
                    new TypeReference<List<RecipeDetailDTO.IngredientItem>>() {});
            }
            if (recipe.getSteps() != null) {
                steps = objectMapper.readValue(recipe.getSteps(),
                    new TypeReference<List<RecipeDetailDTO.StepItem>>() {});
            }
            if (recipe.getTags() != null) {
                tags = objectMapper.readValue(recipe.getTags(), String[].class);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "JSON解析失败");
        }

        return RecipeDetailDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .cover(recipe.getCover())
                .images(images)
                .author(RecipeDetailDTO.AuthorInfo.builder()
                        .id(author != null ? (long) author.getId() : null)
                        .nickname(author != null ? author.getNickname() : "未知用户")
                        .avatar(author != null ? author.getAvatar() : "")
                        .build())
                .ingredients(ingredients)
                .steps(steps)
                .tips(recipe.getTips())
                .cookTime(recipe.getCookTime())
                .difficulty(convertDifficultyToString(recipe.getDifficulty()))
                .calories(recipe.getCalories())
                .tags(tags)
                .stats(RecipeDetailDTO.RecipeStats.builder()
                        .likes(recipe.getLikeCount())
                        .collections(recipe.getCollectionCount())
                        .comments(recipe.getCommentCount())
                        .views(recipe.getViewCount())
                        .build())
                .build();
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

    private Integer convertDifficulty(String difficulty) {
        switch (difficulty) {
            case "simple":
            case "简单": return 1;
            case "medium":
            case "中等": return 2;
            case "hard":
            case "困难": return 3;
            default: return null;
        }
    }

    private String convertDifficultyToString(Integer difficulty) {
        if (difficulty == null) return "";
        switch (difficulty) {
            case 1: return "简单";
            case 2: return "中等";
            case 3: return "困难";
            default: return "未知";
        }
    }
}
