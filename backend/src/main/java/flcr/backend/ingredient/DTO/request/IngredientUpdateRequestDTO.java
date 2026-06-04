package flcr.backend.ingredient.DTO.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class IngredientUpdateRequestDTO {
    @Size(max = 50, message = "食材名称最长50字")
    private String name;

    @Positive(message = "数量必须为正数")
    private BigDecimal quantity;

    @Size(max = 20, message = "单位最长20字")
    private String unit;

    @Size(max = 20, message = "分类最长20字")
    private String category;

    private LocalDate expireDate;

    @Size(max = 50, message = "储存条件最长50字")
    private String storageCondition;
}
