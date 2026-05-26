package flcr.backend.admin.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminCommentUpdateRequestDTO {
    @NotBlank(message = "评论内容不能为空")
    private String content;
}
