package flcr.backend.community.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞/收藏响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeCollectResponseDTO {
    private Boolean isLiked;
    private Integer likeCount;
    private Boolean isCollected;
    private Integer collectionCount;
}
