package flcr.backend.ingredient.DTO.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class IngredientUpdateRequestDTO {
    private String name;
    private BigDecimal quantity;
    private String unit;
    private String category;
    private LocalDate expireDate;
    private String storageCondition;
}
