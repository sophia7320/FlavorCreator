package flcr.backend.admin.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRecipeResponseDTO {
    private Long id;
    private String name;
    private String cover;
    private Long authorId;
    private String authorName;
    private String category;
    private String cookTime;
    private Integer difficulty;
    private Integer calories;
    private Integer source;
    private Integer likeCount;
    private Integer collectionCount;
    private Integer commentCount;
    private Integer viewCount;
    private String createdAt;
    private String updatedAt;
}
