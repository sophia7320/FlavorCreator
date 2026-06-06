package flcr.backend.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.context.UserContext;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.LikeMapper;
import flcr.backend.recipe.DTO.request.ApplyRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.PublishRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.response.ApplyRecipeResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.recipe.service.RecipeService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@DisplayName("菜谱服务实现测试")
class RecipeServiceImplTest {

    @BeforeAll
    static void initMybatisPlusCache() {
        com.baomidou.mybatisplus.core.MybatisConfiguration configuration = new com.baomidou.mybatisplus.core.MybatisConfiguration();
        configuration.setDefaultScriptingLanguage(com.baomidou.mybatisplus.core.MybatisXMLLanguageDriver.class);
        org.apache.ibatis.builder.MapperBuilderAssistant assistant = new org.apache.ibatis.builder.MapperBuilderAssistant(configuration, "");
        assistant.setCurrentNamespace("flcr.backend.recipe.mapper.RecipeMapper");
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(assistant, Recipe.class);
    }

    @Mock private RecipeMapper recipeMapper;
    @Mock private LikeMapper likeMapper;
    @Mock private CollectionMapper collectionMapper;
    @Mock private UserMapper userMapper;
    @Mock private ObjectMapper objectMapper;
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
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(recipeMapper.insert(any(Recipe.class))).thenAnswer(inv -> {
            Recipe r = inv.getArgument(0);
            r.setId(1L);
            return 1;
        });

        PublishRecipeRequestDTO request = new PublishRecipeRequestDTO();
        request.setName("测试菜谱");
        request.setCategory("家常菜");
        request.setCoverUrl("https://example.com/cover.jpg");

        Long id = recipeService.publishRecipe(request);
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
        Long id = recipeService.publishRecipe(request);
        assertEquals(2L, id);
    }

    @Test
    @DisplayName("getRecipeList按分类返回")
    void testGetRecipeList_ReturnsPageByCategory() throws Exception {
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

        Page<RecipeListItemResponseDTO> result = recipeService.getRecipeList(request);
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

        RecipeDetailResponseDTO result = recipeService.getRecipeDetail(1L);
        assertNotNull(result);
        assertEquals("测试菜谱", result.getName());
        assertEquals(101, result.getStats().getViews());
        assertFalse(result.getIsLiked());
    }

    @Test
    @DisplayName("getRecipeDetail不存在抛异常")
    void testGetRecipeDetail_NotFound() {
        when(recipeMapper.selectById(99L)).thenReturn(null);
        assertThrows(flcr.backend.common.exception.BusinessException.class,
                () -> recipeService.getRecipeDetail(99L));
    }

    // ==================== apply 测试 ====================

    @Test
    @DisplayName("apply - 全匹配(100%)返回菜谱")
    void testApply_FullMatch() throws Exception {
        Recipe recipe = buildRecipe(1L);
        recipe.setIngredients("[{\"name\":\"鸡蛋\",\"quantity\":2,\"unit\":\"个\"}]");
        recipe.setAuthorId(999L); // different from USER_ID
        when(recipeMapper.selectList(null)).thenReturn(List.of(recipe));
        when(userMapper.selectById(999L)).thenReturn(buildUser());
        when(objectMapper.readValue(eq(recipe.getIngredients()), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(java.util.Map.of("name", "鸡蛋", "quantity", java.math.BigDecimal.valueOf(2), "unit", "个")));


        ApplyRecipeRequestDTO request = new ApplyRecipeRequestDTO();
        ApplyRecipeRequestDTO.IngredientItem item = new ApplyRecipeRequestDTO.IngredientItem();
        item.setName("鸡蛋");
        item.setQuantity(java.math.BigDecimal.valueOf(3));
        item.setUnit("个");
        request.setIngredients(List.of(item));

        ApplyRecipeResponseDTO result = recipeService.apply(request);

        assertEquals(100, result.getMatchDegree());
        assertFalse(result.getNeedAiGenerate());
        assertEquals(1, result.getRecipes().size());
    }

    @Test
    @DisplayName("apply - 部分匹配(50%)低于阈值返回needAiGenerate")
    void testApply_PartialMatch_BelowThreshold() throws Exception {
        Recipe recipe = buildRecipe(1L);
        recipe.setIngredients("[{\"name\":\"鸡蛋\",\"quantity\":2,\"unit\":\"个\"},{\"name\":\"西红柿\",\"quantity\":1,\"unit\":\"个\"}]");
        recipe.setAuthorId(999L);
        when(recipeMapper.selectList(null)).thenReturn(List.of(recipe));
        when(userMapper.selectById(999L)).thenReturn(buildUser());
        when(objectMapper.readValue(eq(recipe.getIngredients()), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(
                        java.util.Map.of("name", "鸡蛋", "quantity", java.math.BigDecimal.valueOf(2), "unit", "个"),
                        java.util.Map.of("name", "西红柿", "quantity", java.math.BigDecimal.valueOf(1), "unit", "个")));
        when(objectMapper.readValue(eq(recipe.getTags()), eq(String[].class))).thenReturn(new String[]{"家常菜"});

        ApplyRecipeRequestDTO request = new ApplyRecipeRequestDTO();
        ApplyRecipeRequestDTO.IngredientItem item = new ApplyRecipeRequestDTO.IngredientItem();
        item.setName("鸡蛋");
        request.setIngredients(List.of(item));

        ApplyRecipeResponseDTO result = recipeService.apply(request);

        assertEquals(50, result.getMatchDegree());
        assertTrue(result.getNeedAiGenerate());
        assertEquals(1, result.getRecipes().size());
    }

    @Test
    @DisplayName("apply - 烹饪时间偏好过滤")
    void testApply_CookTimeFilter() throws Exception {
        Recipe recipe1 = buildRecipe(1L);
        recipe1.setIngredients("[{\"name\":\"鸡蛋\",\"quantity\":2,\"unit\":\"个\"}]");
        recipe1.setCookTime("15");
        recipe1.setAuthorId(999L);

        Recipe recipe2 = buildRecipe(2L);
        recipe2.setIngredients("[{\"name\":\"鸡蛋\",\"quantity\":2,\"unit\":\"个\"}]");
        recipe2.setCookTime("45");
        recipe2.setAuthorId(999L);

        when(recipeMapper.selectList(null)).thenReturn(List.of(recipe1, recipe2));
        when(userMapper.selectById(999L)).thenReturn(buildUser());
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(java.util.Map.of("name", "鸡蛋", "quantity", java.math.BigDecimal.valueOf(2), "unit", "个")));
        when(objectMapper.readValue(anyString(), eq(String[].class))).thenReturn(new String[]{"快手菜"});
        ApplyRecipeRequestDTO request = new ApplyRecipeRequestDTO();
        ApplyRecipeRequestDTO.IngredientItem item = new ApplyRecipeRequestDTO.IngredientItem();
        item.setName("鸡蛋");
        request.setIngredients(List.of(item));
        ApplyRecipeRequestDTO.Preferences prefs = new ApplyRecipeRequestDTO.Preferences();
        prefs.setCookTime(30);
        request.setPreferences(prefs);
        ApplyRecipeResponseDTO result = recipeService.apply(request);
        assertEquals(100, result.getMatchDegree());
        assertEquals(1, result.getRecipes().size());
        assertEquals(1L, result.getRecipes().get(0).getId()); // recipe1 (15min) passes, recipe2 (45min > 30) filtered
    }

    @Test
    @DisplayName("apply - 难度偏好过滤")
    void testApply_DifficultyFilter() throws Exception {
        Recipe recipe1 = buildRecipe(1L);
        recipe1.setIngredients("[{\"name\":\"鸡蛋\",\"quantity\":2,\"unit\":\"个\"}]");
        recipe1.setDifficulty(1);
        recipe1.setAuthorId(999L);

        Recipe recipe2 = buildRecipe(2L);
        recipe2.setIngredients("[{\"name\":\"鸡蛋\",\"quantity\":2,\"unit\":\"个\"}]");
        recipe2.setDifficulty(3);
        recipe2.setAuthorId(999L);

        when(recipeMapper.selectList(null)).thenReturn(List.of(recipe1, recipe2));
        when(userMapper.selectById(999L)).thenReturn(buildUser());
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(java.util.Map.of("name", "鸡蛋", "quantity", java.math.BigDecimal.valueOf(2), "unit", "个")));
        when(objectMapper.readValue(anyString(), eq(String[].class))).thenReturn(new String[]{"快手菜"});
        ApplyRecipeRequestDTO request = new ApplyRecipeRequestDTO();
        ApplyRecipeRequestDTO.IngredientItem item = new ApplyRecipeRequestDTO.IngredientItem();
        item.setName("鸡蛋");
        request.setIngredients(List.of(item));
        ApplyRecipeRequestDTO.Preferences prefs = new ApplyRecipeRequestDTO.Preferences();
        prefs.setDifficulty(1);
        request.setPreferences(prefs);
        ApplyRecipeResponseDTO result = recipeService.apply(request);
        assertEquals(100, result.getMatchDegree());
        assertEquals(1, result.getRecipes().size());
    }

    @Test
    @DisplayName("apply - 空食材输入返回空")
    void testApply_EmptyIngredients() {
        ApplyRecipeRequestDTO request = new ApplyRecipeRequestDTO();
        request.setIngredients(List.of());
        ApplyRecipeResponseDTO result = recipeService.apply(request);
        assertEquals(0, result.getMatchDegree());
        assertTrue(result.getNeedAiGenerate());
        assertTrue(result.getRecipes().isEmpty());
    }

    @Test
    @DisplayName("apply - 跳过本人菜谱")
    void testApply_SkipOwnRecipe() throws Exception {
        Recipe recipe = buildRecipe(1L);
        recipe.setIngredients("[{\"name\":\"鸡蛋\",\"quantity\":2,\"unit\":\"个\"}]");
        // authorId == USER_ID (1001L)
        when(recipeMapper.selectList(null)).thenReturn(List.of(recipe));
        ApplyRecipeRequestDTO request = new ApplyRecipeRequestDTO();
        ApplyRecipeRequestDTO.IngredientItem item = new ApplyRecipeRequestDTO.IngredientItem();
        item.setName("鸡蛋");
        request.setIngredients(List.of(item));
        ApplyRecipeResponseDTO result = recipeService.apply(request);
        assertTrue(result.getRecipes().isEmpty());
        assertTrue(result.getNeedAiGenerate());
    }

    // ==================== 辅助方法 ====================

    private Recipe buildRecipe(Long id) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setName("测试菜谱");
        recipe.setCover("/uploads/test.jpg");
        recipe.setAuthorId(USER_ID);
        recipe.setCategory("家常菜");
        recipe.setCookTime("15");
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
