package flcr.backend.recipe.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeListItemDTO {
    private Long id;
    private String name;
    private String cover;
    private AuthorInfo author;
    private String cookTime;
    private String difficulty;
    private Integer calories;
    private String[] tags;
    private RecipeStats stats;
    private String createdAt;

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
    public static class RecipeStats {
        private Integer likes;
        private Integer collections;
        private Integer comments;
        private Integer views;
    }
}
