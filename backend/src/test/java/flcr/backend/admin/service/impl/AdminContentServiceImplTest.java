package flcr.backend.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.admin.DTO.request.*;
import flcr.backend.admin.DTO.response.AdminCommentResponseDTO;
import flcr.backend.admin.DTO.response.AdminRecipeResponseDTO;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.community.entity.Comment;
import flcr.backend.community.mapper.CommentMapper;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminContentServiceImplTest {

    @Mock private RecipeMapper recipeMapper;
    @Mock private CommentMapper commentMapper;
    @Mock private UserMapper userMapper;
    @InjectMocks private AdminContentServiceImpl adminContentService;

    @Test
    @DisplayName("getRecipeDetail成功")
    void testGetRecipeDetail_Success() {
        Recipe recipe = buildRecipe(1L);
        when(recipeMapper.selectById(1L)).thenReturn(recipe);
        when(userMapper.selectById(1L)).thenReturn(buildUser(1L));

        AdminRecipeResponseDTO result = adminContentService.getRecipeDetail(1L);
        assertEquals("测试菜谱", result.getName());
        assertEquals(1L, result.getAuthorId());
    }

    @Test
    @DisplayName("getRecipeDetail菜谱不存在抛异常")
    void testGetRecipeDetail_NotFound() {
        when(recipeMapper.selectById(99L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminContentService.getRecipeDetail(99L));
        assertEquals(ResultCode.RESOURCE_NOT_EXIST, ex.getCode());
    }

    @Test
    @DisplayName("createRecipe成功")
    void testCreateRecipe_Success() {
        AdminRecipeCreateRequestDTO request = new AdminRecipeCreateRequestDTO();
        request.setName("新菜谱");
        request.setCookTime("简单");
        request.setDifficulty(1);

        when(recipeMapper.insert(any(Recipe.class))).thenReturn(1);

        AdminRecipeResponseDTO result = adminContentService.createRecipe(request);
        assertEquals("新菜谱", result.getName());
        verify(recipeMapper).insert(any(Recipe.class));
    }

    @Test
    @DisplayName("updateRecipe成功")
    void testUpdateRecipe_Success() {
        Recipe recipe = buildRecipe(1L);
        when(recipeMapper.selectById(1L)).thenReturn(recipe);
        when(userMapper.selectById(1L)).thenReturn(buildUser(1L));
        when(recipeMapper.updateById(any(Recipe.class))).thenReturn(1);

        AdminRecipeUpdateRequestDTO request = new AdminRecipeUpdateRequestDTO();
        request.setName("更新的菜谱");

        AdminRecipeResponseDTO result = adminContentService.updateRecipe(1L, request);
        assertEquals("更新的菜谱", result.getName());
    }

    @Test
    @DisplayName("deleteRecipe成功")
    void testDeleteRecipe_Success() {
        when(recipeMapper.selectById(1L)).thenReturn(buildRecipe(1L));
        when(recipeMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> adminContentService.deleteRecipe(1L));
        verify(recipeMapper).deleteById(1L);
    }

    @Test
    @DisplayName("deleteRecipe不存在抛异常")
    void testDeleteRecipe_NotFound() {
        when(recipeMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> adminContentService.deleteRecipe(99L));
    }

    @Test
    @DisplayName("createComment成功")
    void testCreateComment_Success() {
        when(recipeMapper.selectById(1L)).thenReturn(buildRecipe(1L));
        when(commentMapper.insert(any(Comment.class))).thenReturn(1);

        AdminCommentCreateRequestDTO request = new AdminCommentCreateRequestDTO();
        request.setRecipeId(1L);
        request.setContent("官方评论");

        AdminCommentResponseDTO result = adminContentService.createComment(request);
        assertEquals("官方评论", result.getContent());
        verify(commentMapper).insert(any(Comment.class));
    }

    @Test
    @DisplayName("updateComment成功")
    void testUpdateComment_Success() {
        Comment comment = buildComment(1L);
        when(commentMapper.selectById(1L)).thenReturn(comment);
        when(userMapper.selectById(1L)).thenReturn(buildUser(1L));
        when(recipeMapper.selectById(1L)).thenReturn(buildRecipe(1L));
        when(commentMapper.updateById(any(Comment.class))).thenReturn(1);

        AdminCommentUpdateRequestDTO request = new AdminCommentUpdateRequestDTO();
        request.setContent("更新的评论");

        AdminCommentResponseDTO result = adminContentService.updateComment(1L, request);
        assertEquals("更新的评论", result.getContent());
    }

    @Test
    @DisplayName("deleteComment成功")
    void testDeleteComment_Success() {
        when(commentMapper.selectById(1L)).thenReturn(buildComment(1L));
        when(commentMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> adminContentService.deleteComment(1L));
        verify(commentMapper).deleteById(1L);
    }

    private Recipe buildRecipe(Long id) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setName("测试菜谱");
        recipe.setAuthorId(1L);
        recipe.setCookTime("简单");
        recipe.setDifficulty(1);
        recipe.setLikeCount(10);
        recipe.setCollectionCount(5);
        recipe.setCommentCount(3);
        recipe.setViewCount(100);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());
        return recipe;
    }

    private Comment buildComment(Long id) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setRecipeId(1L);
        comment.setUserId(1L);
        comment.setContent("测试评论");
        comment.setLikeCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        return comment;
    }

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setNickname("测试用户");
        user.setAvatar("/avatar.jpg");
        return user;
    }
}
