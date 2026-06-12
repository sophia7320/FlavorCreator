package flcr.backend.recipe.DTO.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RecipeUpdateRequestDTO {
    @Size(min = 1, max = 128, message = "菜谱名称长度1-128")
    private String name;

    private String coverUrl;

    private List<String> imageUrls;

    private String ingredients;

    private String steps;

    private String tags;

    private String category;

    @Size(max = 255, message = "描述长度不超过255")
    private String desc;

    private String tips;

    @Size(max = 16, message = "烹饪时长不超过16字符")
    private String cookTime;

    @Min(value = 1, message = "难度最小为1")
    @Max(value = 3, message = "难度最大为3")
    private Integer difficulty;

    @Min(value = 0, message = "卡路里不能为负")
    private Integer calories;
}
