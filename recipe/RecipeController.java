import .recipe.RecipeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.UUID;

public class RecipeController {
    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * GET 请求接口
     * 访问示例：http://localhost:8080/api/recipe?food=鸡蛋,西红柿&demand=做一份简单的家常菜
     */
    @GetMapping
    public String getRecipe(
            @RequestParam String food,
            @RequestParam String demand) {

        // 模拟生成 sessionId 和 userId（实际项目中可从请求头或登录信息中获取）
        String sessionId = UUID.randomUUID().toString();
        String userId = "user_001";

        // 调用 Service 层，它会去调用 Python 脚本并记录日志
        return recipeService.generateRecipe(food, demand, sessionId, userId);
    }
}
