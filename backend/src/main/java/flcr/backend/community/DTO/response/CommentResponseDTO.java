package flcr.backend.community.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 评论响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {
    private Long id;
    private UserInfo user;
    private String content;
    private Integer likeCount;
    private Boolean isLiked;
    private String createdAt;
    private List<CommentReplyDTO> replies;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String nickname;
        private String avatar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentReplyDTO {
        private Long id;
        private UserInfo user;
        private String content;
        private String createdAt;
    }
}
