package flcr.backend.recipe.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.context.UserContext;
import flcr.backend.recipe.DTO.request.ApplyRecipeRequestDTO;
import flcr.backend.recipe.DTO.response.ApplyRecipeResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.recipe.service.RecipeMatchService;
import flcr.backend.recipe.util.RecipeDtoAssembler;
import flcr.backend.recipe.util.IngredientSynonyms;
import flcr.backend.recipe.util.TasteTagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeMatchServiceImpl implements RecipeMatchService {

    private final RecipeMapper recipeMapper;
    private final RecipeDtoAssembler recipeDtoAssembler;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;

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

        // 保存原始食材数作为分母（同义词扩展仅用于匹配，不改变分母）
        int originalIngredientCount = userIngredientNames.size();

        Set<String> expandedNames = new HashSet<>(userIngredientNames);
        for (String name : userIngredientNames) {
            expandedNames.addAll(IngredientSynonyms.getSynonyms(name));
        }
        userIngredientNames = expandedNames;

        if (userIngredientNames.isEmpty()) {
            return ApplyRecipeResponseDTO.builder()
                    .matchDegree(0)
                    .recipes(Collections.emptyList())
                    .needAiGenerate(true)
                    .build();
        }

        applyPreferencesFromUserProfile(request);

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
            int matchDegree = matchedNames.size() * 100 / originalIngredientCount;

            ApplyRecipeRequestDTO.Preferences prefs = request.getPreferences();
            if (prefs != null) {
                if (prefs.getCookTime() != null) {
                    Integer recipeCookTime = parseCookTime(recipe.getCookTime());
                    if (recipeCookTime != null && recipeCookTime > prefs.getCookTime()) continue;
                }
                if (prefs.getDifficulty() != null && recipe.getDifficulty() != null) {
                    if (recipe.getDifficulty() > prefs.getDifficulty()) continue;
                }
                matchDegree = applyPreferenceBoost(matchDegree, recipe, prefs);
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
                .map(m -> {
                    RecipeListItemResponseDTO dto = recipeDtoAssembler.convertToListItemDTO(m.recipe());
                    dto.setMatchDegree(m.matchDegree());
                    return dto;
                })
                .collect(Collectors.toList());

        return ApplyRecipeResponseDTO.builder()
                .matchDegree(bestMatchDegree)
                .recipes(recipeDTOs)
                .needAiGenerate(needAiGenerate)
                .build();
    }

    private void applyPreferencesFromUserProfile(ApplyRecipeRequestDTO request) {
        ApplyRecipeRequestDTO.Preferences prefs = request.getPreferences();
        boolean needTaste = prefs == null || prefs.getTaste() == null || prefs.getTaste().isEmpty();
        boolean needDietary = prefs == null || prefs.getDietary() == null || prefs.getDietary().isEmpty();

        if (!needTaste && !needDietary) return;

        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null || user.getPreferences() == null || user.getPreferences().isBlank()) return;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> savedPrefs = objectMapper.readValue(user.getPreferences(), Map.class);
            if (prefs == null) {
                prefs = new ApplyRecipeRequestDTO.Preferences();
                request.setPreferences(prefs);
            }
            if (needTaste && savedPrefs.containsKey("taste")) {
                @SuppressWarnings("unchecked")
                List<String> taste = (List<String>) savedPrefs.get("taste");
                if (taste != null && !taste.isEmpty()) prefs.setTaste(taste);
            }
            if (needDietary && savedPrefs.containsKey("dietary")) {
                @SuppressWarnings("unchecked")
                List<String> dietary = (List<String>) savedPrefs.get("dietary");
                if (dietary != null && !dietary.isEmpty()) prefs.setDietary(dietary);
            }
        } catch (Exception e) {
            log.warn("解析用户偏好失败", e);
        }
    }

    private int applyPreferenceBoost(int baseMatch, Recipe recipe, ApplyRecipeRequestDTO.Preferences prefs) {
        int score = baseMatch;
        boolean hasTasteBoost = false;

        if (prefs.getTaste() != null && !prefs.getTaste().isEmpty()) {
            Set<String> recipeTags = parseTags(recipe.getTags());
            int tasteHits = 0;
            for (String taste : prefs.getTaste()) {
                Set<String> expectedTags = TasteTagMapper.tagsForTaste(taste);
                for (String tag : recipeTags) {
                    if (expectedTags.contains(tag)) {
                        tasteHits++;
                        break;
                    }
                }
            }
            if (tasteHits > 0) {
                int tasteScore = tasteHits * 100 / prefs.getTaste().size();
                score = baseMatch * 70 / 100 + tasteScore * 30 / 100;
                hasTasteBoost = true;
            }
        }

        if (prefs.getDietary() != null && !prefs.getDietary().isEmpty()) {
            for (String dietary : prefs.getDietary()) {
                String expectedCategory = TasteTagMapper.categoryForDietary(dietary);
                if (expectedCategory != null && expectedCategory.equals(recipe.getCategory())) {
                    if (!hasTasteBoost) {
                        score = Math.min(100, baseMatch + 10);
                    }
                    break;
                }
            }
        }

        return Math.min(100, score);
    }

    private Set<String> parseTags(String tagsJson) {
        if (tagsJson == null || tagsJson.isBlank()) return Set.of();
        try {
            String[] tags = objectMapper.readValue(tagsJson, String[].class);
            return Set.of(tags);
        } catch (Exception e) {
            return Set.of();
        }
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
}
