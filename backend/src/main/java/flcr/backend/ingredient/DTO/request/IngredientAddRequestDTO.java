package flcr.backend.ingredient.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class IngredientAddRequestDTO {
    @NotBlank(message = "食材名称不能为空")
    private String name;
    private BigDecimal quantity;
    private String unit;
    private String category;
    private LocalDate expireDate;
    private String storageCondition;
}
