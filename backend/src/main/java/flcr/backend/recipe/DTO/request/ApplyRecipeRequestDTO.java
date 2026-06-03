package flcr.backend.recipe.DTO.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ApplyRecipeRequestDTO {

    private List<IngredientItem> ingredients;
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
    }
}
