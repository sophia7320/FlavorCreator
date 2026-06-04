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
import flcr.backend.common.constants.ImageScene;
import flcr.backend.common.service.ImageUploadService;
import flcr.backend.community.entity.Collection;
import flcr.backend.community.entity.Like;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.LikeMapper;
import flcr.backend.recipe.DTO.request.ApplyRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.PublishRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.response.ApplyRecipeResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeMapper recipeMapper;
    private final LikeMapper likeMapper;
    private final CollectionMapper collectionMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final ImageUploadService imageUploadService;

    @Override
    @Transactional
    public Long publishRecipe(PublishRecipeRequestDTO request, MultipartFile cover,
                              List<MultipartFile> images) {
        Long userId = UserContext.getUserId();

        // Upload cover (validate + store + moderate, auto-cleanup on failure)
        String coverUrl = "";
        if (cover != null) {
            coverUrl = imageUploadService.upload(cover, ImageScene.RECIPE_COVER);
        }

        // Upload images (each independently atomic)
        List<String> imageUrls = new ArrayList<>();
        if (images != null) {
            for (MultipartFile image : images) {
                imageUrls.add(imageUploadService.upload(image, ImageScene.RECIPE_IMAGE));
            }
        }

        // Build and save recipe
        Recipe recipe = buildRecipe(request, coverUrl, imageUrls, userId);
        recipeMapper.insert(recipe);
        return recipe.getId();
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
    public Page<RecipeListItemResponseDTO> getRecipeList(RecipeListRequestDTO request) {
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

        List<RecipeListItemResponseDTO> dtoList = result.getRecords().stream()
                .map(this::convertToListItemDTO)
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

        RecipeDetailResponseDTO dto = convertToDetailDTO(recipe);

        if (userId != null) {
            dto.setIsLiked(checkLiked(userId, recipeId, 1));
            dto.setIsCollected(checkCollected(userId, recipeId));
        } else {
            dto.setIsLiked(false);
            dto.setIsCollected(false);
        }

        return dto;
    }

    @Override
    public ApplyRecipeResponseDTO apply(ApplyRecipeRequestDTO request) {
        Long userId = UserContext.getUserId();

        // Collect user ingredient names
        Set<String> userIngredientNames = new HashSet<>();
        if (request.getIngredients() != null) {
            for (ApplyRecipeRequestDTO.IngredientItem item : request.getIngredients()) {
                if (item.getName() != null && !item.getName().isBlank()) {
                    userIngredientNames.add(item.getName().trim());
                }
            }
        }

        // No ingredients → return empty
        if (userIngredientNames.isEmpty()) {
            return ApplyRecipeResponseDTO.builder()
                    .matchDegree(0)
                    .recipes(Collections.emptyList())
                    .needAiGenerate(true)
                    .build();
        }

        // Get all recipes
        List<Recipe> allRecipes = recipeMapper.selectList(null);

        // Match each recipe
        List<MatchedRecipe> matchedRecipes = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (recipe.getIngredients() == null || recipe.getIngredients().isBlank()) continue;
            if (recipe.getAuthorId() != null && recipe.getAuthorId().equals(userId)) continue; // skip own recipes

            Set<String> recipeIngredientNames = parseIngredientNames(recipe.getIngredients());
            if (recipeIngredientNames.isEmpty()) continue;

            // Calculate match degree
            Set<String> matchedNames = new HashSet<>(userIngredientNames);
            matchedNames.retainAll(recipeIngredientNames);
            int matchDegree = matchedNames.size() * 100 / recipeIngredientNames.size();

            // Preference filtering
            ApplyRecipeRequestDTO.Preferences prefs = request.getPreferences();
            if (prefs != null) {
                if (prefs.getCookTime() != null) {
                    Integer recipeCookTime = parseCookTime(recipe.getCookTime());
                    if (recipeCookTime != null && recipeCookTime > prefs.getCookTime()) continue;
                }
                if (prefs.getDifficulty() != null && recipe.getDifficulty() != null) {
                    if (recipe.getDifficulty() > prefs.getDifficulty()) continue;
                }
            }

            matchedRecipes.add(new MatchedRecipe(recipe, matchDegree));
        }

        // Sort: matchDegree desc, then cookTime asc
        matchedRecipes.sort(Comparator
                .comparingInt(MatchedRecipe::matchDegree).reversed()
                .thenComparingInt(m -> {
                    Integer ct = parseCookTime(m.recipe().getCookTime());
                    return ct != null ? ct : Integer.MAX_VALUE;
                }));

        int bestMatchDegree = matchedRecipes.isEmpty() ? 0 : matchedRecipes.get(0).matchDegree();
        boolean needAiGenerate = bestMatchDegree < 85;

        // Top 5 if below threshold, all if above
        List<MatchedRecipe> resultList = needAiGenerate
                ? matchedRecipes.stream().limit(5).toList()
                : matchedRecipes;

        List<RecipeListItemResponseDTO> recipeDTOs = resultList.stream()
                .map(m -> convertToListItemDTO(m.recipe()))
                .collect(Collectors.toList());

        return ApplyRecipeResponseDTO.builder()
                .matchDegree(bestMatchDegree)
                .recipes(recipeDTOs)
                .needAiGenerate(needAiGenerate)
                .build();
    }

    // ==================== 匹配辅助方法 ====================

    private static record MatchedRecipe(Recipe recipe, int matchDegree) {}

    private Set<String> parseIngredientNames(String ingredientsJson) {
        try {
            List<Map<String, Object>> list = objectMapper.readValue(ingredientsJson,
                    new TypeReference<List<Map<String, Object>>>() {});
            Set<String> names = new HashSet<>();
            for (Map<String, Object> item : list) {
                Object name = item.get("name");
                if (name != null) {
                    names.add(name.toString().trim());
                }
            }
            return names;
        } catch (JsonProcessingException e) {
            log.warn("解析食材JSON失败", e);
            return Collections.emptySet();
        }
    }

    private Integer parseCookTime(String cookTime) {
        if (cookTime == null || cookTime.isBlank()) return null;
        try {
            return Integer.parseInt(cookTime.trim());
        } catch (NumberFormatException e) {
            log.warn("解析烹饪时间失败: {}", cookTime);
            return null;
        }
    }

    // ==================== 私有辅助方法 ====================

    private RecipeListItemResponseDTO convertToListItemDTO(Recipe recipe) {
        User author = userMapper.selectById(recipe.getAuthorId());

        String[] tags = {};
        try {
            if (recipe.getTags() != null) {
                tags = objectMapper.readValue(recipe.getTags(), String[].class);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "JSON解析失败");
        }

        return RecipeListItemResponseDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .cover(recipe.getCover())
                .author(RecipeListItemResponseDTO.AuthorInfo.builder()
                        .id(author != null ? (long) author.getId() : null)
                        .nickname(author != null ? author.getNickname() : "未知用户")
                        .avatar(author != null ? author.getAvatar() : "")
                        .build())
                .cookTime(recipe.getCookTime())
                .difficulty(convertDifficultyToString(recipe.getDifficulty()))
                .calories(recipe.getCalories())
                .tags(tags)
                .stats(RecipeListItemResponseDTO.RecipeStats.builder()
                        .likes(recipe.getLikeCount())
                        .collections(recipe.getCollectionCount())
                        .comments(recipe.getCommentCount())
                        .views(recipe.getViewCount())
                        .build())
                .createdAt(recipe.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    private RecipeDetailResponseDTO convertToDetailDTO(Recipe recipe) {
        User author = userMapper.selectById(recipe.getAuthorId());

        List<String> images = new ArrayList<>();
        try {
            if (recipe.getImages() != null) {
                images = objectMapper.readValue(recipe.getImages(), new TypeReference<List<String>>() {});
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "JSON解析失败");
        }

        List<RecipeDetailResponseDTO.IngredientItem> ingredients = new ArrayList<>();
        List<RecipeDetailResponseDTO.StepItem> steps = new ArrayList<>();
        String[] tags = {};

        try {
            if (recipe.getIngredients() != null) {
                ingredients = objectMapper.readValue(recipe.getIngredients(),
                    new TypeReference<List<RecipeDetailResponseDTO.IngredientItem>>() {});
            }
            if (recipe.getSteps() != null) {
                steps = objectMapper.readValue(recipe.getSteps(),
                    new TypeReference<List<RecipeDetailResponseDTO.StepItem>>() {});
            }
            if (recipe.getTags() != null) {
                tags = objectMapper.readValue(recipe.getTags(), String[].class);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "JSON解析失败");
        }

        return RecipeDetailResponseDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .cover(recipe.getCover())
                .images(images)
                .author(RecipeDetailResponseDTO.AuthorInfo.builder()
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
                .stats(RecipeDetailResponseDTO.RecipeStats.builder()
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
