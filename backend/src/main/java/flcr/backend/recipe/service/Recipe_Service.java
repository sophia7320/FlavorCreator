import flcr.backend.recipe.entity.RecipeRequest;
import flcr.backend.recipe.LLMClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class Recipe_Service {
    private final LLMClient llmClient;

    public RecipeService() {
        this.llmClient = new LLMClient();
    }

    private String askLlm(JSONArray messages) {
        return llmClient.sendPostRequest(messages);
    }

    // 对应 Python 的 is_relevant_question 函数
    private boolean isRelevantQuestion(String ingredientsStr, String preferencesStr) {
        String checkPrompt = String.format(
            "判断%s里面是否完全是食材。，%s里面是否完全是与食谱有关的要求。\n" +
            "只能回答YES或NO。\n" +
            "只有两个都正确时回答YES，否则回答NO。", 
            ingredientsStr, preferencesStr
        );
        
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", checkPrompt);
        messages.put(message);
        
        String result = askLlm(messages);
        return result != null && result.trim().toUpperCase().equals("YES");
    }

    // 对应 Python 的 generate_recipe 函数
    public String generateRecipe(RecipeRequest request) {
        // 将复杂的对象数据转换为字符串，以便进行合规性校验和拼接 Prompt
        String ingredientsStr = request.getIngredients().toString();
        String preferencesStr = request.getPreferences().toString();

        // 业务逻辑：先校验输入是否合规
        if (!isRelevantQuestion(ingredientsStr, preferencesStr)) {
            return "食材输入有误或食谱要求有误，请检查后重试。";
        }

        // 将食材列表格式化为易读的字符串
        StringBuilder ingredientsBuilder = new StringBuilder();
        for (RecipeRequest.Ingredient ing : request.getIngredients()) {
            ingredientsBuilder.append(String.format("- %s: %d %s\n", ing.getName(), ing.getQuantity(), ing.getUnit()));
        }

        // 将偏好对象格式化为易读的字符串
        RecipeRequest.Preferences pref = request.getPreferences();
        String preferencesFormatted = String.format(
            "- 口味: %s\n- 饮食限制: %s\n- 烹饪时间: %d分钟\n- 难度: %s",
            pref.getTaste(), pref.getDietary(), pref.getCookTime(), pref.getDifficulty()
        );

        String prompt = String.format(
            "你是一个专业的食谱问答助手，只回答与flavor creator相关的问题，你的知识范围只包括提供食谱。\n" +
            "以下给可用食材（已包括调料）和食谱要求，请提供几份食谱。\n" +
            "注意：\n" +
            "1.必须严格考虑食材的相冲规则\n" +
            "2.如果用户的问题与产品无关，礼貌拒绝并引导回正题\n" +
            "3.不讨论政治、娱乐、编程、其他竞品等无关话题\n" +
            "4.你的回答不能超过300字\n" +
            "5.你只能使用已有的食材。如果现有食材不能满足食谱所需，你可以自创食谱，但需要标明该食谱为ai自创仅供参考\n" +
            "可用食材：\n%s\n" +
            "食谱要求：\n%s\n" +
            "如果可用食材不足或者可用食材中出现了非食材，请不要提供食谱，只回答“食材输入有误”。\n" +
            "如果食谱要求与食谱无关，请只回答“食材要求有误”。",
            ingredientsBuilder.toString(), preferencesFormatted
        );

        // 使用 org.json 构造 messages 数组
        JSONArray messages = new JSONArray();
        
        JSONObject sysMsg = new JSONObject();
        sysMsg.put("role", "system");
        sysMsg.put("content", prompt);
        messages.put(sysMsg);
        
        JSONObject userMsg1 = new JSONObject();
        userMsg1.put("role", "user");
        userMsg1.put("content", ingredientsBuilder.toString());
        messages.put(userMsg1);
        
        JSONObject userMsg2 = new JSONObject();
        userMsg2.put("role", "user");
        userMsg2.put("content", preferencesFormatted);
        messages.put(userMsg2);

        return askLlm(messages);
    }
}
