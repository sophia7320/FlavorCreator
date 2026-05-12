package flcr.backend.community.controller;

import flcr.backend.common.aop.Public;
import flcr.backend.common.response.Response;
import flcr.backend.community.DTO.request.CommentRequestDTO;
import flcr.backend.community.DTO.response.CommentResponseDTO;
import flcr.backend.community.DTO.response.LikeCollectResponseDTO;
import flcr.backend.community.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/recipe/{id}/like")
    public Response<LikeCollectResponseDTO> likeRecipe(@PathVariable Long id) {
        LikeCollectResponseDTO result = communityService.likeRecipe(id);
        return Response.success(result);
    }

    @DeleteMapping("/recipe/{id}/like")
    public Response<LikeCollectResponseDTO> unlikeRecipe(@PathVariable Long id) {
        LikeCollectResponseDTO result = communityService.unlikeRecipe(id);
        return Response.success(result);
    }

    @PostMapping("/recipe/{id}/collect")
    public Response<LikeCollectResponseDTO> collectRecipe(@PathVariable Long id) {
        LikeCollectResponseDTO result = communityService.collectRecipe(id);
        return Response.success(result);
    }

    @DeleteMapping("/recipe/{id}/collect")
    public Response<LikeCollectResponseDTO> uncollectRecipe(@PathVariable Long id) {
        LikeCollectResponseDTO result = communityService.uncollectRecipe(id);
        return Response.success(result);
    }

    @Public
    @GetMapping("/recipe/{id}/comment")
    public Response<List<CommentResponseDTO>> getComments(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        List<CommentResponseDTO> comments = communityService.getComments(id, page, size);
        return Response.success(comments);
    }

    @PostMapping("/recipe/{id}/comment")
    public Response<CommentResponseDTO> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequestDTO request) {
        CommentResponseDTO comment = communityService.addComment(id, request);
        return Response.success(comment);
    }

    @DeleteMapping("/comment/{id}")
    public Response<Void> deleteComment(@PathVariable Long id) {
        communityService.deleteComment(id);
        return Response.success();
    }

    @PostMapping("/comment/{id}/like")
    public Response<Void> likeComment(@PathVariable Long id) {
        communityService.likeComment(id);
        return Response.success();
    }

    @DeleteMapping("/comment/{id}/like")
    public Response<Void> unlikeComment(@PathVariable Long id) {
        communityService.unlikeComment(id);
        return Response.success();
    }
}
