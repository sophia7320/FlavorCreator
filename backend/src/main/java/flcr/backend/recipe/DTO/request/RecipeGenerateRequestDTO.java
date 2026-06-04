package flcr.backend.recipe.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "菜谱生成请求参数实体")
public class RecipeGenerateRequestDTO {

    @NotEmpty(message = "食材列表不能为空")
    @Valid
    @Schema(description = "食材列表")
    private List<@Valid Ingredient> ingredients;

    @Valid
    @Schema(description = "用户的口味和偏好设置")
    private Preferences preferences;

    @Data
    @NoArgsConstructor
    @Schema(description = "食材详情")
    public static class Ingredient {
        @NotBlank(message = "食材名称不能为空")
        @Schema(description = "食材名称", example = "鸡蛋")
        private String name;

        @Positive(message = "食材数量必须为正数")
        @Schema(description = "食材数量", example = "3")
        private Integer quantity;

        @Schema(description = "食材单位", example = "个")
        private String unit;
    }

    @Data
    @NoArgsConstructor
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
