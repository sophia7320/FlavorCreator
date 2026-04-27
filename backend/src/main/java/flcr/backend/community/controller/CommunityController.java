package flcr.backend.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.common.aop.RequireAuth;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @RequireAuth
    @PostMapping("/recipe")
    public Response<Long> publishRecipe(
            @RequestPart("request") PublishRecipeRequestDTO request,
            @RequestParam("cover") MultipartFile cover,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        Long recipeId = communityService.publishRecipe(request, cover, images);
        return Response.success(recipeId);
    }

    @GetMapping("/recipe/list")
    public Response<Page<RecipeListItemDTO>> getRecipeList(RecipeListRequestDTO request) {
        Page<RecipeListItemDTO> result = communityService.getRecipeList(request);
        return Response.success(result);
    }

    @RequireAuth(required = false)
    @GetMapping("/recipe/{id}")
    public Response<RecipeDetailDTO> getRecipeDetail(@PathVariable Long id) {
        RecipeDetailDTO detail = communityService.getRecipeDetail(id);
        return Response.success(detail);
    }

    @RequireAuth
    @PostMapping("/recipe/{id}/like")
    public Response<LikeCollectResponseDTO> likeRecipe(@PathVariable Long id) {
        LikeCollectResponseDTO result = communityService.likeRecipe(id);
        return Response.success(result);
    }

    @RequireAuth
    @DeleteMapping("/recipe/{id}/like")
    public Response<LikeCollectResponseDTO> unlikeRecipe(@PathVariable Long id) {
        LikeCollectResponseDTO result = communityService.unlikeRecipe(id);
        return Response.success(result);
    }

    @RequireAuth
    @PostMapping("/recipe/{id}/collect")
    public Response<LikeCollectResponseDTO> collectRecipe(@PathVariable Long id) {
        LikeCollectResponseDTO result = communityService.collectRecipe(id);
        return Response.success(result);
    }

    @RequireAuth
    @DeleteMapping("/recipe/{id}/collect")
    public Response<LikeCollectResponseDTO> uncollectRecipe(@PathVariable Long id) {
        LikeCollectResponseDTO result = communityService.uncollectRecipe(id);
        return Response.success(result);
    }

    @RequireAuth(required = false)
    @GetMapping("/recipe/{id}/comment")
    public Response<List<CommentResponseDTO>> getComments(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        List<CommentResponseDTO> comments = communityService.getComments(id, page, size);
        return Response.success(comments);
    }

    @RequireAuth
    @PostMapping("/recipe/{id}/comment")
    public Response<CommentResponseDTO> addComment(
            @PathVariable Long id,
            @RequestBody CommentRequestDTO request) {
        CommentResponseDTO comment = communityService.addComment(id, request);
        return Response.success(comment);
    }

    @RequireAuth
    @DeleteMapping("/comment/{id}")
    public Response<Void> deleteComment(@PathVariable Long id) {
        communityService.deleteComment(id);
        return Response.success();
    }

    @RequireAuth
    @PostMapping("/comment/{id}/like")
    public Response<Void> likeComment(@PathVariable Long id) {
        communityService.likeComment(id);
        return Response.success();
    }

    @RequireAuth
    @DeleteMapping("/comment/{id}/like")
    public Response<Void> unlikeComment(@PathVariable Long id) {
        communityService.unlikeComment(id);
        return Response.success();
    }
}
