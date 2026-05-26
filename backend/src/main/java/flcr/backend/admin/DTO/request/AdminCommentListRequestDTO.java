package flcr.backend.admin.DTO.request;

import lombok.Data;

@Data
public class AdminCommentListRequestDTO {
    private Integer page = 1;
    private Integer size = 20;
    private String keyword;
    private String status;
    private Long recipeId;
}
