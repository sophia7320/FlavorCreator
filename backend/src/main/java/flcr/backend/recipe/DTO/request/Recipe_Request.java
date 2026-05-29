import io.swagger.v3.oas.annotations.media.Schema; // 引入 Swagger 文档注解
import jakarta.validation.Valid; // 引入嵌套校验注解
import jakarta.validation.constraints.NotEmpty; // 引入非空校验注解
import lombok.Data; // 引入 Lombok 简化代码（如果你没装 Lombok 插件，保留你原来的 getter/setter 即可）

import java.util.List;

// @Data 是 Lombok 注解，会自动帮你生成 getter、setter、toString 等方法
// 如果你不使用 Lombok，保留你原本手写的 getter 和 setter 完全没问题！
@Data 
@Schema(description = "菜谱生成请求参数实体") // 接口文档中会显示这个类的说明
public class Recipe_Request {

    @NotEmpty(message = "食材列表不能为空") // 校验：防止前端传空数组
    @Schema(description = "食材列表")
    private List<Ingredient> ingredients;

    @Valid // 开启嵌套校验：确保 Preferences 内部的字段也能被校验到
    @Schema(description = "用户的口味和偏好设置")
    private Preferences preferences;

    // 【补充点】显式提供一个无参构造器，确保 Spring Boot 能正常实例化该对象
    public Recipe_Request() {}

    // 内部类：映射 ingredients 数组中的对象
    @Data
    @Schema(description = "食材详情")
    public static class Ingredient {
        @Schema(description = "食材名称", example = "鸡蛋")
        private String name;
        
        @Schema(description = "食材数量", example = "3")
        private Integer quantity;
        
        @Schema(description = "食材单位", example = "个")
        private String unit;
    }

    // 内部类：映射 preferences 嵌套对象
    @Data
    @Schema(description = "口味与偏好")
    public static class Preferences {
        @Schema(description = "口味要求", example = "[\"清淡\", \"少油\"]")
        private List<String> taste;
        
        @Schema(description = "饮食禁忌/要求", example = "[\"低卡\"]")
        private List<String> dietary;
        
        @Schema(description = "期望烹饪时长（分钟）", example = "30")
        private Integer cookTime;
        
        @Schema(description = "期望难度", example = "简单")
        private String difficulty;
    }
}
