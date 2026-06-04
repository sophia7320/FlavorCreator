package flcr.backend.recipe.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.recipe.DTO.request.RecipeGenerateRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeGenerateResponseDTO;
import flcr.backend.recipe.client.LlmClient;
import flcr.backend.recipe.service.RecipeGenerateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeGenerateServiceImpl implements RecipeGenerateService {

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    @Override
    public RecipeGenerateResponseDTO generateRecipe(RecipeGenerateRequestDTO request) {
        String systemPrompt = buildSystemPrompt(request);
        String llmJsonResponse = llmClient.generateRecipeJson(systemPrompt);
        return parseResponse(llmJsonResponse);
    }

    private String buildSystemPrompt(RecipeGenerateRequestDTO request) {
        StringBuilder ingredientsBuilder = new StringBuilder();
        for (RecipeGenerateRequestDTO.Ingredient ing : request.getIngredients()) {
            ingredientsBuilder.append(String.format(
                    "- %s: %d %s\n",
                    ing.getName(),
                    ing.getQuantity() != null ? ing.getQuantity() : 0,
                    ing.getUnit() != null ? ing.getUnit() : ""
            ));
        }

        RecipeGenerateRequestDTO.Preferences pref = request.getPreferences();
        String taste = listToStr(pref != null ? pref.getTaste() : null);
        String dietary = listToStr(pref != null ? pref.getDietary() : null);
        Integer cookTime = pref != null ? pref.getCookTime() : null;
        String difficulty = pref != null ? pref.getDifficulty() : "";

        String preferencesFormatted = String.format(
                "- 口味: %s\n- 饮食限制: %s\n- 烹饪时间: %s分钟\n- 难度: %s",
                taste,
                dietary,
                cookTime != null ? cookTime.toString() : "不限",
                difficulty != null ? difficulty : "不限"
        );

        return String.format(
                "你是一个专业的食谱问答助手。\n" +
                "可用食材：\n%s\n" +
                "食谱要求：\n%s\n\n" +
                "请根据以上食材和要求，提供一份最佳食谱。\n\n" +
                "【极其重要】你必须且只能返回一个纯 JSON 格式的数据，不要包含任何 markdown 标记（如 ```json）或其他多余文字。\n" +
                "返回的 JSON 必须严格符合以下结构：\n" +
                "{\"recipe\": {\"name\": \"菜谱名\", \"ingredients\": [{\"name\": \"食材名\", \"quantity\": 数量, \"unit\": \"单位\"}], " +
                "\"steps\": [{\"order\": 序号, \"description\": \"步骤描述\"}], \"cookTime\": 烹饪时长, \"difficulty\": \"难度\", " +
                "\"calories\": 卡路里, \"tags\": [\"标签1\", \"标签2\"]}}\n\n" +
                "【注意事项】\n" +
                "1. 必须严格考虑食材的相冲规则，避免推荐相克食材的搭配。\n" +
                "2. 如果用户的问题与食谱产品无关，请礼貌拒绝并引导回正题。\n" +
                "3. 严禁讨论政治、娱乐、编程、其他竞品等无关话题。\n" +
                "4. 你的回答（指生成的JSON内容）必须精炼，不要包含冗长的额外说明。\n" +
                "5. 你只能使用已有的食材。如果现有食材不能满足食谱所需，你可以自创食谱，但需要在 tags 标签中加入 \"AI自创仅供参考\"。\n\n" +
                "如果食材不足或包含非食材，请返回：" +
                "{\"recipe\": {\"name\": \"食材输入有误\", \"ingredients\": [], \"steps\": [], " +
                "\"cookTime\": 0, \"difficulty\": \"\", \"calories\": 0, \"tags\": []}}",
                ingredientsBuilder.toString(), preferencesFormatted
        );
    }

    private RecipeGenerateResponseDTO parseResponse(String llmJsonResponse) {
        try {
            String cleanJson = llmJsonResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
            return objectMapper.readValue(cleanJson, RecipeGenerateResponseDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse LLM JSON response: {}", llmJsonResponse, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "AI生成格式解析失败");
        }
    }

    private static String listToStr(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "无";
        }
        return String.join("、", list);
    }
}
