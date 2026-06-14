package flcr.backend.ingredient.DTO.request;

import lombok.Data;

@Data
public class IngredientListRequestDTO {
    private String sortBy;
    private String sort;
    private Integer status;
    private String category;
}
