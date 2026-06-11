package flcr.backend.ingredient.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientListResponseDTO {
    private List<IngredientResponseDTO> ingredients;
    private Summary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Integer totalCount;
        private Integer expiredCount;
        private Integer urgentCount;
        private Integer warningCount;
        private Integer normalCount;
    }
}
