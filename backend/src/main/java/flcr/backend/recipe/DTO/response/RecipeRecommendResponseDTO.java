package flcr.backend.recipe.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRecommendResponseDTO {
    private String title;
    private List<RecommendItem> recipes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendItem {
        private Long id;
        private String name;
        private String cover;
        private String reason;
    }
}
