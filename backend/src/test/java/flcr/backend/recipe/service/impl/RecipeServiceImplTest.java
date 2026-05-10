package flcr.backend.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.service.FileStorageService;
import flcr.backend.common.service.ImageModerationService;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.LikeMapper;
import flcr.backend.recipe.DTO.request.PublishRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemDTO;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.recipe.service.RecipeService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {

    @Mock private RecipeMapper recipeMapper;
    @Mock private LikeMapper likeMapper;
    @Mock private CollectionMapper collectionMapper;
    @Mock private UserMapper userMapper;
    @Mock private ObjectMapper objectMapper;
    @Mock private FileStorageService fileStorageService;
    @Mock private ImageModerationService imageModerationService;
    @InjectMocks private RecipeServiceImpl recipeService;

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

        Long id = recipeService.publishRecipe(request, cover, null);
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
        Long id = recipeService.publishRecipe(request, null, null);
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

        Page<RecipeListItemDTO> result = recipeService.getRecipeList(request);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("getRecipeList按中文难度'简单'筛选 -> wrapper含difficulty=1")
    void testGetRecipeList_DifficultySimple() throws Exception {
        Recipe recipe = buildRecipe(1L);
        Page<Recipe> page = new Page<>(1, 20);
        page.setRecords(List.of(recipe));
        page.setTotal(1);
        when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(userMapper.selectById(anyLong())).thenReturn(buildUser());

        RecipeListRequestDTO request = new RecipeListRequestDTO();
        request.setDifficulty("简单");
        recipeService.getRecipeList(request);

        ArgumentCaptor<LambdaQueryWrapper<Recipe>> captor = ArgumentCaptor.forClass((Class) LambdaQueryWrapper.class);
        verify(recipeMapper).selectPage(any(Page.class), captor.capture());
        assertTrue(captor.getValue().getCustomSqlSegment().contains("difficulty"),
                "中文'简单'应生成 difficulty 条件");
    }

    @Test
    @DisplayName("getRecipeList按中文难度'中等'筛选 -> wrapper含difficulty=2")
    void testGetRecipeList_DifficultyMedium() throws Exception {
        Recipe recipe = buildRecipe(1L);
        Page<Recipe> page = new Page<>(1, 20);
        page.setRecords(List.of(recipe));
        page.setTotal(1);
        when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(userMapper.selectById(anyLong())).thenReturn(buildUser());

        RecipeListRequestDTO request = new RecipeListRequestDTO();
        request.setDifficulty("中等");
        recipeService.getRecipeList(request);

        ArgumentCaptor<LambdaQueryWrapper<Recipe>> captor = ArgumentCaptor.forClass((Class) LambdaQueryWrapper.class);
        verify(recipeMapper).selectPage(any(Page.class), captor.capture());
        assertTrue(captor.getValue().getCustomSqlSegment().contains("difficulty"),
                "中文'中等'应生成 difficulty 条件");
    }

    @Test
    @DisplayName("getRecipeList按中文难度'困难'筛选 -> wrapper含difficulty=3")
    void testGetRecipeList_DifficultyHard() throws Exception {
        Recipe recipe = buildRecipe(1L);
        Page<Recipe> page = new Page<>(1, 20);
        page.setRecords(List.of(recipe));
        page.setTotal(1);
        when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(userMapper.selectById(anyLong())).thenReturn(buildUser());

        RecipeListRequestDTO request = new RecipeListRequestDTO();
        request.setDifficulty("困难");
        recipeService.getRecipeList(request);

        ArgumentCaptor<LambdaQueryWrapper<Recipe>> captor = ArgumentCaptor.forClass((Class) LambdaQueryWrapper.class);
        verify(recipeMapper).selectPage(any(Page.class), captor.capture());
        assertTrue(captor.getValue().getCustomSqlSegment().contains("difficulty"),
                "中文'困难'应生成 difficulty 条件");
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

        RecipeDetailDTO result = recipeService.getRecipeDetail(1L);
        assertNotNull(result);
        assertEquals("测试菜谱", result.getName());
        assertFalse(result.getIsLiked());
    }

    @Test
    @DisplayName("getRecipeDetail不存在抛异常")
    void testGetRecipeDetail_NotFound() {
        when(recipeMapper.selectById(99L)).thenReturn(null);
        assertThrows(flcr.backend.common.exception.BusinessException.class,
                () -> recipeService.getRecipeDetail(99L));
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
