package flcr.backend.recipe.DTO.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RecipeListRequestDTO {
    private String category;
    private String difficulty;
    private String keyword;

    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 1, message = "每页最小为1")
    private Integer size = 20;
}
