package flcr.backend.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.community.DTO.response.LikeCollectResponseDTO;
import flcr.backend.community.entity.*;
import flcr.backend.community.mapper.*;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityServiceImplTest {

    @Mock private RecipeMapper recipeMapper;
    @Mock private CommentMapper commentMapper;
    @Mock private LikeMapper likeMapper;
    @Mock private CollectionMapper collectionMapper;
    @Mock private UserMapper userMapper;
    @InjectMocks private CommunityServiceImpl communityService;

    private static final Long USER_ID = 1001L;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(USER_ID);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("likeRecipe成功")
    void testLikeRecipe_Success() {
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L, 1L);
        when(likeMapper.insert(any(Like.class))).thenReturn(1);
        when(recipeMapper.selectById(1L)).thenReturn(buildRecipe(1L));
        when(recipeMapper.update(any(), any())).thenReturn(1);
        when(collectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        LikeCollectResponseDTO result = communityService.likeRecipe(1L);
        assertTrue(result.getIsLiked());
    }

    @Test
    @DisplayName("likeRecipe重复点赞抛异常")
    void testLikeRecipe_Duplicate() {
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        BusinessException ex = assertThrows(BusinessException.class, () -> communityService.likeRecipe(1L));
        assertEquals(ResultCode.PARAM_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("likeRecipe并发竞态返回业务异常而非500")
    void testLikeRecipe_RaceCondition() {
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(likeMapper.insert(any(Like.class))).thenThrow(new DuplicateKeyException("Duplicate entry"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> communityService.likeRecipe(1L));
        assertEquals(ResultCode.PARAM_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("unlikeRecipe成功")
    void testUnlikeRecipe_Success() {
        when(likeMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(recipeMapper.selectById(1L)).thenReturn(buildRecipe(1L));
        when(recipeMapper.update(any(), any())).thenReturn(1);
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(collectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertDoesNotThrow(() -> communityService.unlikeRecipe(1L));
    }

    @Test
    @DisplayName("collectRecipe成功")
    void testCollectRecipe_Success() {
        when(collectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L, 1L);
        when(collectionMapper.insert(any(flcr.backend.community.entity.Collection.class))).thenReturn(1);
        when(recipeMapper.selectById(1L)).thenReturn(buildRecipe(1L));
        when(recipeMapper.update(any(), any())).thenReturn(1);
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        LikeCollectResponseDTO result = communityService.collectRecipe(1L);
        assertTrue(result.getIsCollected());
    }

    @Test
    @DisplayName("collectRecipe并发竞态返回业务异常而非500")
    void testCollectRecipe_RaceCondition() {
        when(collectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(collectionMapper.insert(any(flcr.backend.community.entity.Collection.class)))
                .thenThrow(new DuplicateKeyException("Duplicate entry"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> communityService.collectRecipe(1L));
        assertEquals(ResultCode.PARAM_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("deleteComment非本人抛异常")
    void testDeleteComment_NotOwner() {
        Comment comment = new Comment();
        comment.setUserId(999L);
        comment.setRecipeId(1L);
        when(commentMapper.selectById(1L)).thenReturn(comment);

        BusinessException ex = assertThrows(BusinessException.class, () -> communityService.deleteComment(1L));
        assertEquals(ResultCode.PERMISSION_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("deleteComment评论不存在抛异常")
    void testDeleteComment_NotFound() {
        when(commentMapper.selectById(99L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> communityService.deleteComment(99L));
        assertEquals(ResultCode.RESOURCE_NOT_EXIST, ex.getCode());
    }

    @Test
    @DisplayName("likeComment成功")
    void testLikeComment_Success() {
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(likeMapper.insert(any(Like.class))).thenReturn(1);
        when(commentMapper.update(any(), any())).thenReturn(1);

        assertDoesNotThrow(() -> communityService.likeComment(1L));
        verify(likeMapper).insert(any(Like.class));
        verify(commentMapper).update(any(), any());
    }

    @Test
    @DisplayName("likeComment重复点赞抛异常")
    void testLikeComment_Duplicate() {
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> communityService.likeComment(1L));
        assertEquals(ResultCode.PARAM_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("unlikeComment成功")
    void testUnlikeComment_Success() {
        when(likeMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(commentMapper.update(any(), any())).thenReturn(1);

        assertDoesNotThrow(() -> communityService.unlikeComment(1L));
        verify(commentMapper).update(any(), any());
    }

    private Recipe buildRecipe(Long id) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setName("测试菜谱");
        recipe.setCover("/uploads/test.jpg");
        recipe.setAuthorId(USER_ID);
        recipe.setCategory("家常菜");
        recipe.setCookTime(30);
        recipe.setDifficulty(1);
        recipe.setCalories(300);
        recipe.setLikeCount(10);
        recipe.setCollectionCount(5);
        recipe.setCommentCount(3);
        recipe.setViewCount(100);
        recipe.setSource(2);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());
        return recipe;
    }
}
