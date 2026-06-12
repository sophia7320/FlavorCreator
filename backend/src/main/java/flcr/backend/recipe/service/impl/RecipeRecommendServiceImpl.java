package flcr.backend.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.context.UserContext;
import flcr.backend.recipe.DTO.response.RecipeRecommendResponseDTO;
import flcr.backend.recipe.client.LlmClient;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.recipe.service.RecipeRecommendService;
import flcr.backend.recipe.util.DifficultyUtil;
import flcr.backend.user.DTO.request.UpdateUserInfoRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeRecommendServiceImpl implements RecipeRecommendService {

    private final RecipeMapper recipeMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final LlmClient llmClient;

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
                    sb.append(" | ").append(DifficultyUtil.convertDifficultyToString(r.getDifficulty()));
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
        sb.append("{\"recipes\":[{\"id\":1,\"reason\":\"推荐理由\"}]}\n");
        if (candidates.isEmpty()) {
            sb.append("候选池为空时，生成1道推荐菜谱，id为0，reason以\"[AI生成]\"开头，name和reason自行拟定。\n");
        }

        return sb.toString();
    }

    private RecipeRecommendResponseDTO parseRecommendResponse(String llmResponse, List<Recipe> candidates) {
        Map<Long, Recipe> recipeMap = new HashMap<>();
        for (Recipe r : candidates) {
            recipeMap.put(r.getId(), r);
        }

        try {
            java.util.regex.Matcher m = Pattern.compile("\\{.*}", Pattern.DOTALL).matcher(llmResponse);
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
        for (int i = 0; i < Math.min(1, sorted.size()); i++) {
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
        for (int i = 0; i < Math.min(1, candidates.size()); i++) {
            Recipe r = candidates.get(i);
            items.add(RecipeRecommendResponseDTO.RecommendItem.builder()
                    .id(r.getId()).name(r.getName()).cover(r.getCover()).reason("大家最近都在做").build());
        }
        return RecipeRecommendResponseDTO.builder()
                .title("今天吃什么？")
                .recipes(items)
                .build();
    }
}
