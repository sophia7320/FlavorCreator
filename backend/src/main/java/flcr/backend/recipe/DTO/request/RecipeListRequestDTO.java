package flcr.backend.recipe.DTO.request;

import lombok.Data;

@Data
public class RecipeListRequestDTO {
    private String category;
    private String difficulty;
    private String taste;
    private String keyword;
    private Integer page = 1;
    private Integer size = 20;
}
