package flcr.backend.community.DTO.request;

import lombok.Data;

/**
 * 发表评论请求DTO
 */
@Data
public class CommentRequestDTO {
    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID（回复时为必填）
     */
    private Long parentId;
}
