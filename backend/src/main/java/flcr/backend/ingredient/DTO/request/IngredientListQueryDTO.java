package flcr.backend.ingredient.DTO.request;

import lombok.Data;

@Data
public class IngredientListQueryDTO {
    private String sortBy;
    private String sort;
    private String status;
    private String category;
}
