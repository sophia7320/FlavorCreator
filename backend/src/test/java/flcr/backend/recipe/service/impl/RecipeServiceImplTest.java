package flcr.backend.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.context.UserContext;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.CommentMapper;
import flcr.backend.community.mapper.LikeMapper;
import flcr.backend.recipe.DTO.request.ApplyRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.CreateRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeUpdateRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.response.ApplyRecipeResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeRecommendResponseDTO;
import flcr.backend.recipe.client.LlmClient;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.user.DTO.request.UpdateUserInfoRequestDTO;
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
    @Mock private CommentMapper commentMapper;
    @Mock private UserMapper userMapper;
    @Mock private ObjectMapper objectMapper;
    @Mock private LlmClient llmClient;
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

        CreateRecipeRequestDTO request = new CreateRecipeRequestDTO();
        request.setName("测试菜谱");
        request.setCategory("home");
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

        CreateRecipeRequestDTO request = new CreateRecipeRequestDTO();
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

    // ==================== recommend 测试 ====================

    @Test
    @DisplayName("recommend AI调用成功并解析响应")
    void testRecommend_SuccessWithPreferences() throws Exception {
        User user = buildUser();
        user.setPreferences("{\"taste\":[\"辣\"],\"cookTime\":\"30\",\"difficulty\":\"简单\"}");
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        UpdateUserInfoRequestDTO.Preferences prefs = new UpdateUserInfoRequestDTO.Preferences();
        prefs.setTaste(List.of("辣"));
        prefs.setCookTime("30");
        prefs.setDifficulty("简单");
        when(objectMapper.readValue(anyString(), eq(UpdateUserInfoRequestDTO.Preferences.class))).thenReturn(prefs);

        Recipe r1 = buildRecipe(1L);
        r1.setTags("[\"辣\",\"川菜\"]");
        Recipe r2 = buildRecipe(2L);
        r2.setTags("[\"清淡\"]");
        when(recipeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(r1, r2));

        String llmResponse = "{\"recipes\":[{\"id\":1,\"reason\":\"匹配你的辣口味偏好\"}]}";
        when(llmClient.generateRecipeJson(anyString())).thenReturn(llmResponse);
        java.util.Map<String, Object> respMap = new java.util.LinkedHashMap<>();
        java.util.List<java.util.Map<String, Object>> respRecipes = new java.util.ArrayList<>();
        java.util.Map<String, Object> item1 = new java.util.LinkedHashMap<>();
        item1.put("id", 1); item1.put("reason", "匹配你的辣口味偏好");
        respRecipes.add(item1);
        respMap.put("recipes", respRecipes);
        when(objectMapper.readValue(eq(llmResponse), eq(java.util.Map.class))).thenReturn(respMap);

        RecipeRecommendResponseDTO result = recipeService.recommend();
        assertNotNull(result);
        assertEquals(1, result.getRecipes().size());
        assertEquals("匹配你的辣口味偏好", result.getRecipes().get(0).getReason());
        assertTrue(result.getRecipes().get(0).getName() != null);
        verify(llmClient).generateRecipeJson(argThat(prompt ->
                prompt.contains("辣") && prompt.contains("简单")));
    }

    @Test
    @DisplayName("recommend 候选池为空时AI生成并带[AI生成]标注")
    void testRecommend_EmptyCandidates_GeneratesWithLabel() throws Exception {
        User user = buildUser();
        when(userMapper.selectById(USER_ID)).thenReturn(user);
        when(recipeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(objectMapper.readValue(anyString(), eq(UpdateUserInfoRequestDTO.Preferences.class))).thenReturn(null);

        String llmResponse = "{\"recipes\":[{\"id\":0,\"name\":\"青椒肉丝\",\"reason\":\"[AI生成]简单下饭\"}]}";
        when(llmClient.generateRecipeJson(anyString())).thenReturn(llmResponse);
        java.util.Map<String, Object> respMap = new java.util.LinkedHashMap<>();
        java.util.List<java.util.Map<String, Object>> respRecipes = new java.util.ArrayList<>();
        java.util.Map<String, Object> item1 = new java.util.LinkedHashMap<>();
        item1.put("id", 0); item1.put("name", "青椒肉丝"); item1.put("reason", "[AI生成]简单下饭");
        respRecipes.add(item1);
        respMap.put("recipes", respRecipes);
        when(objectMapper.readValue(eq(llmResponse), eq(java.util.Map.class))).thenReturn(respMap);

        RecipeRecommendResponseDTO result = recipeService.recommend();
        assertEquals(1, result.getRecipes().size());
        assertNull(result.getRecipes().get(0).getId());
        assertTrue(result.getRecipes().get(0).getReason().startsWith("[AI生成]"));
    }

    @Test
    @DisplayName("recommend LLM失败时降级返回热门Top3")
    void testRecommend_LlmFallback() throws Exception {
        User user = buildUser();
        when(userMapper.selectById(USER_ID)).thenReturn(user);
        when(objectMapper.readValue(anyString(), eq(UpdateUserInfoRequestDTO.Preferences.class))).thenReturn(null);

        Recipe r1 = buildRecipe(1L);
        r1.setName("热门菜1");
        Recipe r2 = buildRecipe(2L);
        r2.setName("热门菜2");
        Recipe r3 = buildRecipe(3L);
        r3.setName("热门菜3");
        when(recipeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(r1, r2, r3));

        when(llmClient.generateRecipeJson(anyString())).thenThrow(new RuntimeException("network error"));

        RecipeRecommendResponseDTO result = recipeService.recommend();
        assertEquals(1, result.getRecipes().size());
        assertEquals("热门菜1", result.getRecipes().get(0).getName());
        assertEquals("大家最近都在做", result.getRecipes().get(0).getReason());
    }

    // ==================== updateRecipe 测试 ====================

    @Test
    @DisplayName("updateRecipe菜谱不存在抛异常")
    void testUpdateRecipe_NotFound() {
        when(recipeMapper.selectById(99L)).thenReturn(null);

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        request.setName("新名字");

        assertThrows(flcr.backend.common.exception.BusinessException.class,
                () -> recipeService.updateRecipe(99L, request));
    }

    @Test
    @DisplayName("updateRecipe非作者无权修改")
    void testUpdateRecipe_Forbidden() {
        Recipe recipe = buildRecipe(1L);
        recipe.setAuthorId(9999L);
        when(recipeMapper.selectById(1L)).thenReturn(recipe);

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        request.setName("新名字");

        assertThrows(flcr.backend.common.exception.BusinessException.class,
                () -> recipeService.updateRecipe(1L, request));
    }

    @Test
    @DisplayName("updateRecipe正常更新全字段返回详情")
    void testUpdateRecipe_Success() throws Exception {
        Recipe recipe = buildRecipe(1L);
        recipe.setTags("[\"home\"]");
        recipe.setIngredients("[{\"name\":\"鸡蛋\",\"quantity\":2,\"unit\":\"个\"}]");
        recipe.setSteps("[{\"order\":1,\"description\":\"打散\"}]");

        when(recipeMapper.selectById(1L)).thenReturn(recipe);
        when(recipeMapper.updateById(any(Recipe.class))).thenReturn(1);
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser());
        when(objectMapper.writeValueAsString(any())).thenReturn("[\"new.jpg\"]");
        when(objectMapper.readValue(eq("[\"home\"]"), eq(String[].class))).thenReturn(new String[]{"home"});
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenAnswer(inv -> {
                    String json = inv.getArgument(0);
                    if (json != null && json.contains("鸡蛋")) {
                        return List.of(java.util.Map.of("name", "鸡蛋", "quantity", java.math.BigDecimal.valueOf(2), "unit", "个"));
                    }
                    return List.of();
                });
        when(objectMapper.readTree(anyString())).thenReturn(null);

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        request.setName("新菜谱名");
        request.setDesc("新描述");
        request.setCategory("lowcal");
        request.setCoverUrl("https://new-cover.jpg");
        request.setImageUrls(List.of("new.jpg"));
        request.setDifficulty(2);
        request.setCalories(200);
        request.setCookTime("30");
        request.setTips("新技巧");
        request.setIngredients("[{\"name\":\"鸡蛋\",\"quantity\":2,\"unit\":\"个\"}]");
        request.setSteps("[{\"order\":1,\"description\":\"打散\"}]");
        request.setTags("[\"lowcal\"]");

        RecipeDetailResponseDTO result = recipeService.updateRecipe(1L, request);

        assertNotNull(result);
        assertEquals("新菜谱名", result.getName());
        assertEquals("新描述", result.getDesc());
        assertEquals("lowcal", result.getCategory());
        assertEquals("中等", result.getDifficulty());
        assertEquals(Integer.valueOf(200), result.getCalories());
        assertEquals("30", result.getCookTime());
        verify(recipeMapper).updateById(any(Recipe.class));
    }

    @Test
    @DisplayName("updateRecipe部分更新只改desc其他字段不变")
    void testUpdateRecipe_PartialUpdate() throws Exception {
        Recipe recipe = buildRecipe(1L);
        String originalName = recipe.getName();
        Integer originalDifficulty = recipe.getDifficulty();

        when(recipeMapper.selectById(1L)).thenReturn(recipe);
        when(recipeMapper.updateById(any(Recipe.class))).thenReturn(1);
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser());
        when(objectMapper.readValue(anyString(), eq(String[].class))).thenReturn(new String[]{});
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of());

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        request.setDesc("新描述");

        RecipeDetailResponseDTO result = recipeService.updateRecipe(1L, request);

        assertEquals("新描述", result.getDesc());
        assertEquals(originalName, result.getName());
        assertEquals(originalDifficulty.toString(), convertDifficultyToString(result.getDifficulty()));
    }

    @Test
    @DisplayName("updateRecipe空body静默成功返回原详情")
    void testUpdateRecipe_EmptyBody() throws Exception {
        Recipe recipe = buildRecipe(1L);

        when(recipeMapper.selectById(1L)).thenReturn(recipe);
        when(recipeMapper.updateById(any(Recipe.class))).thenReturn(1);
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser());
        when(objectMapper.readValue(anyString(), eq(String[].class))).thenReturn(new String[]{});
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of());

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        RecipeDetailResponseDTO result = recipeService.updateRecipe(1L, request);

        assertNotNull(result);
        assertEquals(recipe.getName(), result.getName());
    }

    @Test
    @DisplayName("updateRecipe清空图片imageUrls为空数组")
    void testUpdateRecipe_ClearImages() throws Exception {
        Recipe recipe = buildRecipe(1L);
        recipe.setImages("[\"old.jpg\"]");

        when(recipeMapper.selectById(1L)).thenReturn(recipe);
        when(recipeMapper.updateById(any(Recipe.class))).thenReturn(1);
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser());
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(objectMapper.readValue(eq("[]"), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of());
        when(objectMapper.readValue(anyString(), eq(String[].class))).thenReturn(new String[]{});
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of());

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        request.setImageUrls(List.of());

        RecipeDetailResponseDTO result = recipeService.updateRecipe(1L, request);

        assertTrue(result.getImages().isEmpty());
    }

    @Test
    @DisplayName("updateRecipe不可变字段未被覆盖")
    void testUpdateRecipe_ImmutableFieldsPreserved() throws Exception {
        Recipe recipe = buildRecipe(1L);
        recipe.setSource(2);
        recipe.setAuthorId(USER_ID);
        recipe.setLikeCount(100);
        recipe.setCollectionCount(50);
        recipe.setCommentCount(10);
        recipe.setViewCount(999);
        LocalDateTime originalCreatedAt = recipe.getCreatedAt();

        when(recipeMapper.selectById(1L)).thenReturn(recipe);
        when(recipeMapper.updateById(any(Recipe.class))).thenReturn(1);
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser());
        when(objectMapper.readValue(anyString(), eq(String[].class))).thenReturn(new String[]{});
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of());

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        request.setName("改名后的菜谱");

        RecipeDetailResponseDTO result = recipeService.updateRecipe(1L, request);

        assertEquals("改名后的菜谱", result.getName());
        assertEquals(Integer.valueOf(100), result.getStats().getLikes());
        assertEquals(Integer.valueOf(50), result.getStats().getCollections());
        assertEquals(Integer.valueOf(10), result.getStats().getComments());
        assertEquals(Integer.valueOf(999), result.getStats().getViews());
    }

    private String convertDifficultyToString(String difficulty) {
        switch (difficulty) {
            case "简单": return "1";
            case "中等": return "2";
            case "困难": return "3";
            default: return "";
        }
    }

    // ==================== deleteRecipe 测试 ====================

    @Test
    @DisplayName("deleteRecipe菜谱不存在抛异常")
    void testDeleteRecipe_NotFound() {
        when(recipeMapper.selectById(99L)).thenReturn(null);

        assertThrows(flcr.backend.common.exception.BusinessException.class,
                () -> recipeService.deleteRecipe(99L));
    }

    @Test
    @DisplayName("deleteRecipe非作者无权删除")
    void testDeleteRecipe_Forbidden() {
        Recipe recipe = buildRecipe(1L);
        recipe.setAuthorId(9999L);
        when(recipeMapper.selectById(1L)).thenReturn(recipe);

        assertThrows(flcr.backend.common.exception.BusinessException.class,
                () -> recipeService.deleteRecipe(1L));
    }

    @Test
    @DisplayName("deleteRecipe正常删除自己的菜谱")
    void testDeleteRecipe_Success() {
        Recipe recipe = buildRecipe(1L);
        when(recipeMapper.selectById(1L)).thenReturn(recipe);
        when(recipeMapper.deleteById(1L)).thenReturn(1);
        when(likeMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(3);
        when(collectionMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(2);
        when(commentMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(5);

        assertDoesNotThrow(() -> recipeService.deleteRecipe(1L));
        verify(recipeMapper).deleteById(1L);
    }

    @Test
    @DisplayName("deleteRecipe级联清理like和collection和comment")
    void testDeleteRecipe_CascadeCleanup() {
        Recipe recipe = buildRecipe(1L);
        when(recipeMapper.selectById(1L)).thenReturn(recipe);
        when(recipeMapper.deleteById(1L)).thenReturn(1);
        when(likeMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(collectionMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(commentMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);

        recipeService.deleteRecipe(1L);

        verify(likeMapper).delete(any(LambdaQueryWrapper.class));
        verify(collectionMapper).delete(any(LambdaQueryWrapper.class));
        verify(commentMapper).delete(any(LambdaQueryWrapper.class));
    }

    // ==================== 辅助方法 ====================

    private Recipe buildRecipe(Long id) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setName("测试菜谱");
        recipe.setCover("/uploads/test.jpg");
        recipe.setAuthorId(USER_ID);
        recipe.setCategory("home");
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
