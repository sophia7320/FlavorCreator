package flcr.backend.community.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequestDTO {
    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long parentId;
}
