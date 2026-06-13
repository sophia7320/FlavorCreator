package flcr.backend.recipe.DTO.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ApplyRecipeRequestDTO {

    @NotEmpty(message = "食材列表不能为空")
    @Valid
    private List<@Valid IngredientItem> ingredients;

    private Preferences preferences;

    @Data
    @NoArgsConstructor
    public static class IngredientItem {
        private String name;
        private BigDecimal quantity;
        private String unit;
    }

    @Data
    @NoArgsConstructor
    public static class Preferences {
        private Integer cookTime;
        private Integer difficulty;
        private List<String> taste;
        private List<String> dietary;
    }
}
