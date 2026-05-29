package flcr.backend.admin.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentResponseDTO {
    private Long id;
    private Long recipeId;
    private String recipeName;
    private Long userId;
    private String userName;
    private String content;
    private Long parentId;
    private Integer likeCount;
    private String createdAt;
    private String updatedAt;
}
