package flcr.backend.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.community.DTO.request.CommentRequestDTO;
import flcr.backend.community.DTO.request.PublishRecipeRequestDTO;
import flcr.backend.community.DTO.request.RecipeListRequestDTO;
import flcr.backend.community.DTO.response.CommentResponseDTO;
import flcr.backend.community.DTO.response.LikeCollectResponseDTO;
import flcr.backend.community.DTO.response.RecipeDetailDTO;
import flcr.backend.community.DTO.response.RecipeListItemDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CommunityService {

    Long publishRecipe(PublishRecipeRequestDTO request, MultipartFile cover, List<MultipartFile> images);

    Page<RecipeListItemDTO> getRecipeList(RecipeListRequestDTO request);

    RecipeDetailDTO getRecipeDetail(Long recipeId);

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
