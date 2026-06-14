package flcr.backend.recipe.service.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        RecipeGenerateResponseDTO result = parseResponse(llmJsonResponse);
        if (result == null || result.getRecipe() == null) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "AI生成内容为空");
        }
        return result;
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
                "【输出格式铁律】\n" +
                "你的回复必须以 { 开头、以 } 结尾，前后没有任何其他字符。\n" +
                "以下回复都是错误的，会导致解析失败：\n" +
                "  ✗ \"好的，以下是为您生成的食谱：{...}\"\n" +
                "  ✗ \"```json\\n{...}\\n```\"\n" +
                "  ✗ \"{...}希望您喜欢！\"\n" +
                "正确做法：整个回复就是一个裸 JSON，如 {\"recipe\":{...}}\n\n" +
                "返回的 JSON 必须严格符合以下结构及类型：\n" +
                "{\"recipe\": {\"name\": \"菜谱名\", " +
                "\"desc\": \"一句话简介\", " +
                "\"ingredients\": [{\"name\": \"食材名\", \"quantity\": 3, \"unit\": \"个\"}], " +
                "\"steps\": [{\"order\": 1, \"description\": \"步骤描述\"}], " +
                "\"cookTime\": 30, \"difficulty\": \"简单\", " +
                "\"calories\": 280, \"tags\": [\"快手菜\"]}}\n\n" +
                "【字段类型约束——违反将导致解析失败】\n" +
                "- desc: 一句话简介，不超过50字\n" +
                "- cookTime: 纯数字，表示分钟数，不要带\"分钟\"等任何单位。正确: 30  错误: \"30分钟\"\n" +
                "- calories: 纯数字，不要带\"大卡\"、\"kcal\"等单位。正确: 280  错误: \"280大卡\"\n" +
                "- quantity: 纯数字，不要带单位。正确: 3  错误: \"3个\"\n" +
                "- order: 纯数字，不要带\"步骤\"等前缀。正确: 1  错误: \"步骤1\"\n\n" +
                "【步骤写作规范】\n" +
                "每一步需包含以下要素：\n" +
                "- 火候：大火/中火/小火\n" +
                "- 大致时长：如\"翻炒约2分钟\"\n" +
                "- 判断标准：如\"炒至变色\"、\"煮至沸腾\"、\"煎至两面金黄\"\n" +
                "- 不要只写\"翻炒均匀\"或\"煮熟即可\"这种空泛描述\n\n" +
                "【食材原则】\n" +
                "基础调料（油、盐、酱油、醋、糖、料酒、葱、姜、蒜、辣椒、花椒、淀粉）默认可用，无需在食材列表中列出。\n" +
                "请尽量使用用户提供的主要食材设计菜谱，非必要不增加额外主要食材。\n" +
                "如确需补充主要食材，在 tags 中标注 \"AI 补充食材\"。\n\n" +
                "【注意事项】\n" +
                "1. 必须严格考虑食材的相冲规则，避免推荐相克食材的搭配。\n" +
                "2. 如果用户的问题与食谱产品无关，请礼貌拒绝并引导回正题。\n" +
                "3. 严禁讨论政治、娱乐、编程、其他竞品等无关话题。\n" +
                "4. 如果食材不足或包含非食材，请返回：" +
                "{\"recipe\": {\"name\": \"食材输入有误\", \"ingredients\": [], \"steps\": [], " +
                "\"cookTime\": 0, \"difficulty\": \"\", \"calories\": 0, \"tags\": []}}",
                ingredientsBuilder.toString(), preferencesFormatted);
    }

    private static final Pattern JSON_BLOCK = Pattern.compile("\\{.*}", Pattern.DOTALL);

    private RecipeGenerateResponseDTO parseResponse(String llmJsonResponse) {
        try {
            String cleanJson = llmJsonResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
            return objectMapper.readValue(cleanJson, RecipeGenerateResponseDTO.class);
        } catch (JsonProcessingException e) {
            // 兜底：正则提取第一个 { 到最后一个 } 之间的 JSON
            Matcher m = JSON_BLOCK.matcher(llmJsonResponse);
            if (m.find()) {
                try {
                    return objectMapper.readValue(m.group(), RecipeGenerateResponseDTO.class);
                } catch (JsonProcessingException ignored) {
                }
            }
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
