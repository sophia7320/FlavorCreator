import flcr.backend.recipe.entity.RecipeRequest;
import flcr.backend.recipe.service.RecipeService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Recipe_Controller {
    
    private final RecipeService recipeService;

    public RecipeController() {
        this.recipeService = new RecipeService();
    }

    // 【差异点2：主程序入口】
    public static void main(String[] args) {
        RecipeController controller = new RecipeController();
        controller.handleRecipeRequest();
    }

    public void handleRecipeRequest() {
        try {
            String jsonFilePath = "config.json"; 
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)), StandardCharsets.UTF_8);
            JSONObject config = new JSONObject(jsonContent);
            
            // 手动解析复杂的嵌套 JSON 结构并映射到 DTO
            RecipeRequest request = new RecipeRequest();
            
            // 1. 解析 ingredients 数组
            JSONArray ingredientsJson = config.getJSONArray("ingredients");
            List<RecipeRequest.Ingredient> ingredientsList = new ArrayList<>();
            for (int i = 0; i < ingredientsJson.length(); i++) {
                JSONObject ingObj = ingredientsJson.getJSONObject(i);
                RecipeRequest.Ingredient ingredient = new RecipeRequest.Ingredient();
                ingredient.setName(ingObj.getString("name"));
                ingredient.setQuantity(ingObj.getInt("quantity"));
                ingredient.setUnit(ingObj.getString("unit"));
                ingredientsList.add(ingredient);
            }
            request.setIngredients(ingredientsList);
            
            // 2. 解析 preferences 嵌套对象
            JSONObject prefJson = config.getJSONObject("preferences");
            RecipeRequest.Preferences preferences = new RecipeRequest.Preferences();
            
            // 解析数组 (taste 和 dietary)
            JSONArray tasteJson = prefJson.getJSONArray("taste");
            List<String> tasteList = new ArrayList<>();
            for (int i = 0; i < tasteJson.length(); i++) tasteList.add(tasteJson.getString(i));
            preferences.setTaste(tasteList);

            JSONArray dietaryJson = prefJson.getJSONArray("dietary");
            List<String> dietaryList = new ArrayList<>();
            for (int i = 0; i < dietaryJson.length(); i++) dietaryList.add(dietaryJson.getString(i));
            preferences.setDietary(dietaryList);
            
            // 解析普通字段
            preferences.setCookTime(prefJson.getInt("cookTime"));
            preferences.setDifficulty(prefJson.getString("difficulty"));
            
            request.setPreferences(preferences);

            // 调用 Service 层处理业务
            String result = recipeService.generateRecipe(request);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
