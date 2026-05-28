package flcr.backend.admin.DTO.request;

import lombok.Data;

@Data
public class AdminRecipeUpdateRequestDTO {
    private String name;
    private String cover;
    private String images;
    private String ingredients;
    private String steps;
    private String tips;
    private String cookTime;
    private Integer difficulty;
    private Integer calories;
    private String tags;
    private String category;
}
