package flcr.backend.recipe.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PublishRecipeRequestDTO {
    @NotBlank(message = "菜谱名称不能为空")
    private String name;
    @NotBlank(message = "食材信息不能为空")
    private String ingredients;
    @NotBlank(message = "步骤信息不能为空")
    private String steps;
    private String tags;
    private String category;
    private String tips;
    private Integer cookTime;
    private Integer difficulty;
    private Integer calories;
}
