package flcr.backend.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.community.DTO.request.*;
import flcr.backend.community.DTO.response.*;
import flcr.backend.community.service.CommunityService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityControllerTest {

    @Mock private CommunityService communityService;
    @InjectMocks private CommunityController controller;

    @Test
    @DisplayName("发布菜谱成功返回id")
    void testPublishRecipe() {
        MultipartFile cover = mock(MultipartFile.class);
        List<MultipartFile> images = List.of(mock(MultipartFile.class));
        when(communityService.publishRecipe(any(), eq(cover), eq(images))).thenReturn(1L);

        assertEquals(1L, controller.publishRecipe(new PublishRecipeRequestDTO(), cover, images).getData());
    }

    @Test
    @DisplayName("发布菜谱无图片")
    void testPublishRecipe_NoImages() {
        MultipartFile cover = mock(MultipartFile.class);
        when(communityService.publishRecipe(any(), eq(cover), isNull())).thenReturn(2L);

        assertEquals(2L, controller.publishRecipe(new PublishRecipeRequestDTO(), cover, null).getData());
    }

    @Test
    @DisplayName("菜谱列表返回分页")
    void testGetRecipeList() {
        Page<RecipeListItemDTO> page = new Page<>(1, 20);
        when(communityService.getRecipeList(any())).thenReturn(page);

        assertEquals(200, controller.getRecipeList(new RecipeListRequestDTO()).getCode());
    }

    @Test
    @DisplayName("菜谱详情返回")
    void testGetRecipeDetail() {
        RecipeDetailDTO detail = RecipeDetailDTO.builder().id(1L).name("测试").build();
        when(communityService.getRecipeDetail(1L)).thenReturn(detail);

        assertEquals("测试", controller.getRecipeDetail(1L).getData().getName());
    }

    @Test
    @DisplayName("点赞返回状态")
    void testLikeRecipe() {
        LikeCollectResponseDTO rsp = LikeCollectResponseDTO.builder().isLiked(true).likeCount(1).build();
        when(communityService.likeRecipe(1L)).thenReturn(rsp);

        assertTrue(controller.likeRecipe(1L).getData().getIsLiked());
    }

    @Test
    @DisplayName("取消点赞")
    void testUnlikeRecipe() {
        LikeCollectResponseDTO rsp = LikeCollectResponseDTO.builder().isLiked(false).build();
        when(communityService.unlikeRecipe(1L)).thenReturn(rsp);

        assertFalse(controller.unlikeRecipe(1L).getData().getIsLiked());
    }

    @Test
    @DisplayName("收藏返回状态")
    void testCollectRecipe() {
        LikeCollectResponseDTO rsp = LikeCollectResponseDTO.builder().isCollected(true).collectionCount(1).build();
        when(communityService.collectRecipe(1L)).thenReturn(rsp);

        assertTrue(controller.collectRecipe(1L).getData().getIsCollected());
    }

    @Test
    @DisplayName("评论列表返回")
    void testGetComments() {
        when(communityService.getComments(1L, 1, 20)).thenReturn(List.of());
        assertEquals(200, controller.getComments(1L, 1, 20).getCode());
    }

    @Test
    @DisplayName("发表评论成功")
    void testAddComment() {
        CommentRequestDTO req = new CommentRequestDTO();
        req.setContent("好菜谱");
        CommentResponseDTO rsp = CommentResponseDTO.builder().content("好菜谱").build();
        when(communityService.addComment(1L, req)).thenReturn(rsp);

        assertEquals("好菜谱", controller.addComment(1L, req).getData().getContent());
    }

    @Test
    @DisplayName("删除评论成功")
    void testDeleteComment() {
        assertDoesNotThrow(() -> controller.deleteComment(1L));
        verify(communityService).deleteComment(1L);
    }
}
