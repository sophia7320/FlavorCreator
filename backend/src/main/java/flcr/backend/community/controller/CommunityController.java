package flcr.backend.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.common.aop.RequireAuth;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.response.Response;
import flcr.backend.community.DTO.request.CommentRequestDTO;
import flcr.backend.community.DTO.request.PublishRecipeRequestDTO;
import flcr.backend.community.DTO.request.RecipeListRequestDTO;
import flcr.backend.community.DTO.response.CommentResponseDTO;
import flcr.backend.community.DTO.response.LikeCollectResponseDTO;
import flcr.backend.community.DTO.response.RecipeDetailDTO;
import flcr.backend.community.DTO.response.RecipeListItemDTO;
import flcr.backend.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 社区控制器
 */
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    /**
     * 发布菜谱
     */
    @RequireAuth
    @PostMapping("/recipe")
    public Response<Long> publishRecipe(
            PublishRecipeRequestDTO request,
            @RequestParam("cover") MultipartFile cover,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        Long recipeId = communityService.publishRecipe(request, cover, images, UserContext.getUserId());
        return Response.success(recipeId);
    }

    /**
     * 获取菜谱列表
     */
    @GetMapping("/recipe/list")
    public Response<Page<RecipeListItemDTO>> getRecipeList(RecipeListRequestDTO request) {
        Page<RecipeListItemDTO> result = communityService.getRecipeList(request);
        return Response.success(result);
    }

    /**
     * 获取菜谱详情
     */
    @RequireAuth(required = false)
    @GetMapping("/recipe/{id}")
    public Response<RecipeDetailDTO> getRecipeDetail(@PathVariable Long id) {
        RecipeDetailDTO detail = communityService.getRecipeDetail(id, UserContext.getUserId());
        return Response.success(detail);
    }

    /**
     * 点赞菜谱
     */
    @RequireAuth
    @PostMapping("/recipe/{id}/like")
    public Response<LikeCollectResponseDTO> likeRecipe(@PathVariable Long id) {
        LikeCollectResponseDTO result = communityService.likeRecipe(id, UserContext.getUserId());
        return Response.success(result);
    }

    /**
     * 取消点赞菜谱
     */
    @RequireAuth
    @DeleteMapping("/recipe/{id}/like")
    public Response<LikeCollectResponseDTO> unlikeRecipe(@PathVariable Long id) {
        LikeCollectResponseDTO result = communityService.unlikeRecipe(id, UserContext.getUserId());
        return Response.success(result);
    }

    /**
     * 收藏菜谱
     */
    @RequireAuth
    @PostMapping("/recipe/{id}/collect")
    public Response<LikeCollectResponseDTO> collectRecipe(@PathVariable Long id) {
        LikeCollectResponseDTO result = communityService.collectRecipe(id, UserContext.getUserId());
        return Response.success(result);
    }

    /**
     * 取消收藏菜谱
     */
    @RequireAuth
    @DeleteMapping("/recipe/{id}/collect")
    public Response<LikeCollectResponseDTO> uncollectRecipe(@PathVariable Long id) {
        LikeCollectResponseDTO result = communityService.uncollectRecipe(id, UserContext.getUserId());
        return Response.success(result);
    }

    /**
     * 获取评论列表
     */
    @RequireAuth(required = false)
    @GetMapping("/recipe/{id}/comment")
    public Response<List<CommentResponseDTO>> getComments(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        List<CommentResponseDTO> comments = communityService.getComments(id, page, size, UserContext.getUserId());
        return Response.success(comments);
    }

    /**
     * 发表评论
     */
    @RequireAuth
    @PostMapping("/recipe/{id}/comment")
    public Response<CommentResponseDTO> addComment(
            @PathVariable Long id,
            @RequestBody CommentRequestDTO request) {
        CommentResponseDTO comment = communityService.addComment(id, request, UserContext.getUserId());
        return Response.success(comment);
    }

    /**
     * 删除评论
     */
    @RequireAuth
    @DeleteMapping("/comment/{id}")
    public Response<Void> deleteComment(@PathVariable Long id) {
        communityService.deleteComment(id, UserContext.getUserId());
        return Response.success();
    }

    /**
     * 点赞评论
     */
    @RequireAuth
    @PostMapping("/comment/{id}/like")
    public Response<Void> likeComment(@PathVariable Long id) {
        communityService.likeComment(id, UserContext.getUserId());
        return Response.success();
    }

    /**
     * 取消点赞评论
     */
    @RequireAuth
    @DeleteMapping("/comment/{id}/like")
    public Response<Void> unlikeComment(@PathVariable Long id) {
        communityService.unlikeComment(id, UserContext.getUserId());
        return Response.success();
    }
}