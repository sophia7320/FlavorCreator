package flcr.backend.recipe.DTO.request;

import lombok.Data;

@Data
public class PublishRecipeRequestDTO {
    private String name;
    private String ingredients;
    private String steps;
    private String tags;
    private String category;
    private String tips;
    private Integer cookTime;
    private Integer difficulty;
    private Integer calories;
}
