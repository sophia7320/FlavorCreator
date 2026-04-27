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

/**
 * 社区服务接口
 */
public interface CommunityService {

    /**
     * 发布菜谱
     */
    Long publishRecipe(PublishRecipeRequestDTO request, MultipartFile cover, List<MultipartFile> images, Long userId);

    /**
     * 获取菜谱列表
     */
    Page<RecipeListItemDTO> getRecipeList(RecipeListRequestDTO request);

    /**
     * 获取菜谱详情
     */
    RecipeDetailDTO getRecipeDetail(Long recipeId, Long userId);

    /**
     * 点赞菜谱
     */
    LikeCollectResponseDTO likeRecipe(Long recipeId, Long userId);

    /**
     * 取消点赞菜谱
     */
    LikeCollectResponseDTO unlikeRecipe(Long recipeId, Long userId);

    /**
     * 收藏菜谱
     */
    LikeCollectResponseDTO collectRecipe(Long recipeId, Long userId);

    /**
     * 取消收藏菜谱
     */
    LikeCollectResponseDTO uncollectRecipe(Long recipeId, Long userId);

    /**
     * 获取评论列表
     */
    List<CommentResponseDTO> getComments(Long recipeId, Integer page, Integer size, Long userId);

    /**
     * 发表评论
     */
    CommentResponseDTO addComment(Long recipeId, CommentRequestDTO request, Long userId);

    /**
     * 删除评论
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 点赞评论
     */
    void likeComment(Long commentId, Long userId);

    /**
     * 取消点赞评论
     */
    void unlikeComment(Long commentId, Long userId);
}
