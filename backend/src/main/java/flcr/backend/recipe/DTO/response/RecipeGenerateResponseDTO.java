package flcr.backend.recipe.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜谱生成响应数据实体")
public class RecipeGenerateResponseDTO {

    @Schema(description = "生成的菜谱详情对象")
    private RecipeDetail recipe;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "菜谱详细详情")
    public static class RecipeDetail {

        @Schema(description = "菜谱名称", example = "青椒炒鸡蛋")
        private String name;

        @Schema(description = "所需食材列表（包含AI补充的调料）")
        private List<IngredientItem> ingredients;

        @Schema(description = "烹饪步骤列表")
        private List<StepItem> steps;

        @Schema(description = "烹饪时长（分钟）", example = "10")
        private Integer cookTime;

        @Schema(description = "烹饪难度", example = "简单")
        private String difficulty;

        @Schema(description = "卡路里", example = "280")
        private Integer calories;

        @Schema(description = "菜谱标签", example = "[\"AI 生成\", \"快手菜\"]")
        private List<String> tags;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "菜谱中的食材项")
    public static class IngredientItem {
        @Schema(description = "食材名称", example = "鸡蛋")
        private String name;

        @Schema(description = "食材数量", example = "3")
        private Integer quantity;

        @Schema(description = "食材单位", example = "个")
        private String unit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "菜谱中的步骤项")
    public static class StepItem {
        @Schema(description = "步骤序号", example = "1")
        private Integer order;

        @Schema(description = "步骤详细描述", example = "鸡蛋打散，加少许盐搅拌均匀")
        private String description;
    }
}
