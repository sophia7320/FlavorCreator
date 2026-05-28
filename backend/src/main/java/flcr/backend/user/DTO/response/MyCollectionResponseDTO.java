package flcr.backend.user.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCollectionResponseDTO {
    private Long id;
    private Long recipeId;
    private String recipeName;
    private String cover;
    private Long authorId;
    private String authorName;
    private String collectedAt;
}
