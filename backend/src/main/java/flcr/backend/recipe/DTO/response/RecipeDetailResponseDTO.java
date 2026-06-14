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
public class RecipeDetailResponseDTO {
    private Long id;
    private String name;
    private String cover;
    private List<String> images;
    private AuthorInfo author;
    private List<IngredientItem> ingredients;
    private List<StepItem> steps;
    private String desc;
    private String tips;
    private String cookTime;
    private String difficulty;
    private Integer calories;
    private String[] tags;
    private String category;
    private RecipeStats stats;
    private Boolean isLiked;
    private Boolean isCollected;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String nickname;
        private String avatar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientItem {
        private String name;
        private Double quantity;
        private String unit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepItem {
        private Integer order;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeStats {
        private Integer likes;
        private Integer collections;
        private Integer comments;
        private Integer views;
    }
}
