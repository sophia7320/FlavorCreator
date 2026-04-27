package flcr.backend.ingredient.DTO.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class IngredientBatchAddRequestDTO {
    private List<BatchItem> ingredients;

    @Data
    @NoArgsConstructor
    public static class BatchItem {
        private String name;
        private BigDecimal quantity;
        private String unit;
        private String category;
        private LocalDate expireDate;
    }
}
