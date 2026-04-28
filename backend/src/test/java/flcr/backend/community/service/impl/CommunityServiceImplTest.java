package flcr.backend.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.service.FileStorageService;
import flcr.backend.community.DTO.request.*;
import flcr.backend.community.DTO.response.*;
import flcr.backend.community.entity.*;
import flcr.backend.community.mapper.*;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
class CommunityServiceImplTest {

    @Mock private RecipeMapper recipeMapper;
    @Mock private CommentMapper commentMapper;
    @Mock private LikeMapper likeMapper;
    @Mock private CollectionMapper collectionMapper;
    @Mock private UserMapper userMapper;
    @Mock private ObjectMapper objectMapper;
    @Mock private FileStorageService fileStorageService;
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
    @DisplayName("publishRecipe成功返回recipeId")
    void testPublishRecipe_Success() throws Exception {
        MultipartFile cover = mock(MultipartFile.class);
        when(fileStorageService.store(cover, "recipe-cover")).thenReturn("/uploads/test.jpg");
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(recipeMapper.insert(any(Recipe.class))).thenAnswer(inv -> {
            Recipe r = inv.getArgument(0);
            r.setId(1L);
            return 1;
        });

        PublishRecipeRequestDTO request = new PublishRecipeRequestDTO();
        request.setName("测试菜谱");
        request.setCategory("家常菜");

        Long id = communityService.publishRecipe(request, cover, null);
        assertEquals(1L, id);
    }

    @Test
    @DisplayName("publishRecipe封面为空")
    void testPublishRecipe_NoCover() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(recipeMapper.insert(any(Recipe.class))).thenAnswer(inv -> {
            Recipe r = inv.getArgument(0);
            r.setId(2L);
            return 1;
        });

        PublishRecipeRequestDTO request = new PublishRecipeRequestDTO();
        request.setName("无图菜谱");
        Long id = communityService.publishRecipe(request, null, null);
        assertEquals(2L, id);
    }

    @Test
    @DisplayName("getRecipeList按分类返回")
    void testGetRecipeList() throws Exception {
        Recipe recipe = buildRecipe(1L);
        recipe.setTags("[\"家常菜\"]");
        Page<Recipe> page = new Page<>(1, 20);
        page.setRecords(List.of(recipe));
        page.setTotal(1);
        when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(userMapper.selectById(anyLong())).thenReturn(buildUser());
        when(objectMapper.readValue(anyString(), eq(String[].class))).thenReturn(new String[]{"家常菜"});

        RecipeListRequestDTO request = new RecipeListRequestDTO();
        request.setPage(1);
        request.setSize(20);

        Page<RecipeListItemDTO> result = communityService.getRecipeList(request);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("getRecipeDetail存在返回详情")
    void testGetRecipeDetail_Found() {
        Recipe recipe = buildRecipe(1L);
        when(recipeMapper.selectById(1L)).thenReturn(recipe);
        when(userMapper.selectById(anyLong())).thenReturn(buildUser());
        when(recipeMapper.update(any(), any())).thenReturn(1);
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(collectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        RecipeDetailDTO result = communityService.getRecipeDetail(1L);
        assertNotNull(result);
        assertEquals("测试菜谱", result.getName());
        assertFalse(result.getIsLiked());
    }

    @Test
    @DisplayName("getRecipeDetail不存在抛异常")
    void testGetRecipeDetail_NotFound() {
        when(recipeMapper.selectById(99L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> communityService.getRecipeDetail(99L));
        assertEquals(ResultCode.RESOURCE_NOT_EXIST, ex.getCode());
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

    private User buildUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setNickname("测试用户");
        user.setAvatar("https://example.com/avatar.jpg");
        return user;
    }
}
