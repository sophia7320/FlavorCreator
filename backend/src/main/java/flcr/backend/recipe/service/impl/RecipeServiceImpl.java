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
import flcr.backend.recipe.DTO.response.RecipeRecommendResponseDTO;
import flcr.backend.recipe.client.LlmClient;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.recipe.service.RecipeService;
import flcr.backend.user.DTO.request.UpdateUserInfoRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final LlmClient llmClient;

    @Override
    @Transactional
    public Long publishRecipe(PublishRecipeRequestDTO request) {
        Long userId = UserContext.getUserId();
        Recipe recipe = buildRecipe(request, userId);
        recipeMapper.insert(recipe);
        return recipe.getId();
    }

    private Recipe buildRecipe(PublishRecipeRequestDTO request, Long userId) {
        Recipe recipe = new Recipe();
        recipe.setName(request.getName());
        recipe.setCover(request.getCoverUrl());
        try {
            recipe.setImages(objectMapper.writeValueAsString(request.getImageUrls()));
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
        recipe.setViewCount(recipe.getViewCount() + 1);

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
    public RecipeRecommendResponseDTO recommend() {
        Long userId = UserContext.getUserId();

        User user = userMapper.selectById(userId);
        String preferencesStr = user != null ? user.getPreferences() : null;
        UpdateUserInfoRequestDTO.Preferences prefs = parsePreferences(preferencesStr);

        List<Recipe> candidates = queryCandidates();
        String prompt = buildRecommendPrompt(prefs, candidates);
        String llmResponse;
        try {
            llmResponse = llmClient.generateRecipeJson(prompt);
        } catch (Exception e) {
            log.warn("LLM调用失败，使用降级策略", e);
            return fallbackRecommend(prefs, candidates);
        }

        return parseRecommendResponse(llmResponse, candidates);
    }

    private UpdateUserInfoRequestDTO.Preferences parsePreferences(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, UpdateUserInfoRequestDTO.Preferences.class);
        } catch (Exception e) {
            log.warn("用户偏好解析失败", e);
            return null;
        }
    }

    private List<Recipe> queryCandidates() {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Recipe::getLikeCount);
        wrapper.last("LIMIT 20");
        return recipeMapper.selectList(wrapper);
    }

    private String buildRecommendPrompt(UpdateUserInfoRequestDTO.Preferences prefs, List<Recipe> candidates) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个食谱推荐助手。根据用户偏好从候选菜谱中选出最合适的3个，并给出推荐理由。\n\n");

        sb.append("用户偏好：\n");
        if (prefs != null) {
            if (prefs.getTaste() != null && !prefs.getTaste().isEmpty()) {
                sb.append("- 口味：").append(String.join("、", prefs.getTaste())).append("\n");
            }
            if (prefs.getDietary() != null && !prefs.getDietary().isEmpty()) {
                sb.append("- 饮食限制：").append(String.join("、", prefs.getDietary())).append("\n");
            }
            if (prefs.getCookTime() != null && !prefs.getCookTime().isBlank()) {
                sb.append("- 烹饪时间：").append(prefs.getCookTime()).append("分钟内\n");
            }
            if (prefs.getDifficulty() != null && !prefs.getDifficulty().isBlank()) {
                sb.append("- 难度：").append(prefs.getDifficulty()).append("\n");
            }
        }
        if (prefs == null) {
            sb.append("- 无特殊偏好\n");
        }

        sb.append("\n候选菜谱：\n");
        if (candidates.isEmpty()) {
            sb.append("（无候选菜谱，请自行推荐3道适合的菜谱）\n");
        } else {
            for (Recipe r : candidates) {
                sb.append("[").append(r.getId()).append("] ").append(r.getName());
                if (r.getCategory() != null) sb.append(" | ").append(r.getCategory());
                if (r.getCookTime() != null) sb.append(" | ").append(r.getCookTime()).append("分钟");
                if (r.getDifficulty() != null) {
                    String diffStr = convertDifficultyToString(r.getDifficulty());
                    sb.append(" | ").append(diffStr);
                }
                sb.append("\n");
                if (r.getTags() != null) {
                    try {
                        String[] tags = objectMapper.readValue(r.getTags(), String[].class);
                        if (tags.length > 0) {
                            sb.append("  标签：").append(String.join("、", tags)).append("\n");
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        sb.append("\n返回JSON（必须纯JSON，不要任何其他文字）：\n");
        sb.append("{\"recipes\":[{\"id\":1,\"reason\":\"推荐理由\"},{\"id\":2,\"reason\":\"推荐理由\"},{\"id\":3,\"reason\":\"推荐理由\"}]}\n");
        if (candidates.isEmpty()) {
            sb.append("候选池为空时，生成3道推荐菜谱，id为0，reason以\"[AI生成]\"开头，name和reason自行拟定。\n");
        }

        return sb.toString();
    }

    private RecipeRecommendResponseDTO parseRecommendResponse(String llmResponse, List<Recipe> candidates) {
        Map<Long, Recipe> recipeMap = new HashMap<>();
        for (Recipe r : candidates) {
            recipeMap.put(r.getId(), r);
        }

        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\{.*}", java.util.regex.Pattern.DOTALL).matcher(llmResponse);
            if (m.find()) {
                llmResponse = m.group();
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> raw = objectMapper.readValue(llmResponse, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> recipes = (List<Map<String, Object>>) raw.get("recipes");

            List<RecipeRecommendResponseDTO.RecommendItem> items = new ArrayList<>();
            for (Map<String, Object> item : recipes) {
                Object idObj = item.get("id");
                long recId = (idObj instanceof Number) ? ((Number) idObj).longValue() : 0L;
                String reason = (String) item.get("reason");
                String name = (String) item.get("name");

                if (recId > 0 && recipeMap.containsKey(recId)) {
                    Recipe r = recipeMap.get(recId);
                    items.add(RecipeRecommendResponseDTO.RecommendItem.builder()
                            .id(r.getId()).name(r.getName()).cover(r.getCover()).reason(reason).build());
                } else {
                    items.add(RecipeRecommendResponseDTO.RecommendItem.builder()
                            .id(null).name(name != null ? name : "AI推荐").cover(null).reason(reason).build());
                }
            }

            return RecipeRecommendResponseDTO.builder()
                    .title("今天吃什么？")
                    .recipes(items)
                    .build();
        } catch (Exception e) {
            log.error("LLM推荐响应解析失败", e);
            return fallbackRecommendFromResponse(recipeMap);
        }
    }

    private RecipeRecommendResponseDTO fallbackRecommendFromResponse(Map<Long, Recipe> recipeMap) {
        List<Recipe> sorted = new ArrayList<>(recipeMap.values());
        List<RecipeRecommendResponseDTO.RecommendItem> items = new ArrayList<>();
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            Recipe r = sorted.get(i);
            items.add(RecipeRecommendResponseDTO.RecommendItem.builder()
                    .id(r.getId()).name(r.getName()).cover(r.getCover()).reason("大家最近都在做").build());
        }
        return RecipeRecommendResponseDTO.builder()
                .title("今天吃什么？")
                .recipes(items)
                .build();
    }

    private RecipeRecommendResponseDTO fallbackRecommend(UpdateUserInfoRequestDTO.Preferences prefs, List<Recipe> candidates) {
        if (candidates.isEmpty()) {
            return RecipeRecommendResponseDTO.builder()
                    .title("今天吃什么？")
                    .recipes(List.of())
                    .build();
        }

        List<RecipeRecommendResponseDTO.RecommendItem> items = new ArrayList<>();
        for (int i = 0; i < Math.min(3, candidates.size()); i++) {
            Recipe r = candidates.get(i);
            items.add(RecipeRecommendResponseDTO.RecommendItem.builder()
                    .id(r.getId()).name(r.getName()).cover(r.getCover()).reason("大家最近都在做").build());
        }
        return RecipeRecommendResponseDTO.builder()
                .title("今天吃什么？")
                .recipes(items)
                .build();
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
