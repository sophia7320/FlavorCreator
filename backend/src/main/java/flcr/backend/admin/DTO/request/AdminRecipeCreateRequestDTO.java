package flcr.backend.admin.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminRecipeCreateRequestDTO {
    @NotBlank(message = "菜谱名称不能为空")
    private String name;

    private String cover;
    private String images;
    private String ingredients;
    private String steps;
    private String tips;

    @NotBlank(message = "烹饪方式不能为空")
    private String cookTime;

    @NotNull(message = "难度不能为空")
    private Integer difficulty;

    private Integer calories;
    private String tags;
    private String category;
    private Integer source;
}
