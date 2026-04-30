package flcr.backend.community.service;

import flcr.backend.community.DTO.request.CommentRequestDTO;
import flcr.backend.community.DTO.response.CommentResponseDTO;
import flcr.backend.community.DTO.response.LikeCollectResponseDTO;

import java.util.List;

public interface CommunityService {

    LikeCollectResponseDTO likeRecipe(Long recipeId);

    LikeCollectResponseDTO unlikeRecipe(Long recipeId);

    LikeCollectResponseDTO collectRecipe(Long recipeId);

    LikeCollectResponseDTO uncollectRecipe(Long recipeId);

    List<CommentResponseDTO> getComments(Long recipeId, Integer page, Integer size);

    CommentResponseDTO addComment(Long recipeId, CommentRequestDTO request);

    void deleteComment(Long commentId);

    void likeComment(Long commentId);

    void unlikeComment(Long commentId);
}
