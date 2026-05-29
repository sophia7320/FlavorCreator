package flcr.backend.ingredient.DTO.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class IngredientBatchAddRequestDTO {
    @NotEmpty(message = "食材列表不能为空")
    @Valid
    private List<BatchItem> ingredients;

    @Data
    @NoArgsConstructor
    public static class BatchItem {
        @NotBlank(message = "食材名称不能为空")
        private String name;
        private BigDecimal quantity;
        private String unit;
        private String category;
        private LocalDate expireDate;
    }
}
