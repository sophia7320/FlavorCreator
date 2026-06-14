package flcr.backend.recipe.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.context.UserContext;
import flcr.backend.recipe.DTO.request.ApplyRecipeRequestDTO;
import flcr.backend.recipe.DTO.response.ApplyRecipeResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.recipe.util.RecipeDtoAssembler;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeMatchServiceImpl 食材匹配服务测试")
class RecipeMatchServiceImplTest {

    @Mock
    private RecipeMapper recipeMapper;

    @Mock
    private RecipeDtoAssembler recipeDtoAssembler;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private RecipeMatchServiceImpl recipeMatchService;

    private static final Long USER_ID = 1001L;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(USER_ID);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ==================== 辅助方法 ====================

    private ApplyRecipeRequestDTO createRequest(List<String> ingredientNames) {
        return createRequest(ingredientNames, null);
    }

    private ApplyRecipeRequestDTO createRequest(List<String> ingredientNames, ApplyRecipeRequestDTO.Preferences prefs) {
        ApplyRecipeRequestDTO request = new ApplyRecipeRequestDTO();
        if (ingredientNames != null) {
            List<ApplyRecipeRequestDTO.IngredientItem> items = ingredientNames.stream()
                    .map(name -> {
                        ApplyRecipeRequestDTO.IngredientItem item = new ApplyRecipeRequestDTO.IngredientItem();
                        item.setName(name);
                        return item;
                    })
                    .toList();
            request.setIngredients(items);
        }
        request.setPreferences(prefs);
        return request;
    }

    private ApplyRecipeRequestDTO.Preferences prefs(Integer cookTime, Integer difficulty,
                                                    List<String> taste, List<String> dietary) {
        ApplyRecipeRequestDTO.Preferences p = new ApplyRecipeRequestDTO.Preferences();
        p.setCookTime(cookTime);
        p.setDifficulty(difficulty);
        p.setTaste(taste);
        p.setDietary(dietary);
        return p;
    }

    private Recipe recipe(Long id, Long authorId, String ingredientsJson,
                          String cookTime, Integer difficulty, String tags, String category) {
        Recipe r = new Recipe();
        r.setId(id);
        r.setAuthorId(authorId);
        r.setIngredients(ingredientsJson);
        r.setCookTime(cookTime);
        r.setDifficulty(difficulty);
        r.setTags(tags);
        r.setCategory(category);
        r.setName("菜谱" + id);
        return r;
    }

    private RecipeListItemResponseDTO dto(Long id) {
        return RecipeListItemResponseDTO.builder().id(id).name("菜谱" + id).build();
    }

    /** 创建一个 [{name}, {name}, …] 的食材 JSON */
    private String ingredientsJson(String... names) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < names.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("{\"name\":\"").append(names[i]).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    /** 将食材名列表转为 objectMapper.readValue 返回的 List<Map> */
    private List<Map<String, Object>> ingredientMaps(String... names) {
        return List.of(names).stream().map(n -> Map.<String, Object>of("name", n)).toList();
    }

    // ==================== 空/边缘用例 ====================

    @Test
    @DisplayName("空食材列表返回 matchDegree=0, recipes=[], needAiGenerate=true")
    void testApply_EmptyIngredients_ReturnsEmptyResult() {
        ApplyRecipeResponseDTO response = recipeMatchService.apply(createRequest(List.of()));

        assertEquals(0, response.getMatchDegree());
        assertTrue(response.getRecipes().isEmpty());
        assertTrue(response.getNeedAiGenerate());
        verify(recipeMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("null 食材列表返回 matchDegree=0, recipes=[], needAiGenerate=true")
    void testApply_NullIngredients_ReturnsEmptyResult() {
        ApplyRecipeResponseDTO response = recipeMatchService.apply(createRequest(null));

        assertEquals(0, response.getMatchDegree());
        assertTrue(response.getRecipes().isEmpty());
        assertTrue(response.getNeedAiGenerate());
        verify(recipeMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("空字符串和空白食材名被过滤后返回空结果")
    void testApply_BlankIngredientNames_FilteredToEmpty() {
        ApplyRecipeRequestDTO request = new ApplyRecipeRequestDTO();
        ApplyRecipeRequestDTO.IngredientItem i1 = new ApplyRecipeRequestDTO.IngredientItem();
        i1.setName("");
        ApplyRecipeRequestDTO.IngredientItem i2 = new ApplyRecipeRequestDTO.IngredientItem();
        i2.setName("   ");
        request.setIngredients(List.of(i1, i2));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(0, response.getMatchDegree());
        assertTrue(response.getRecipes().isEmpty());
        assertTrue(response.getNeedAiGenerate());
        verify(recipeMapper, never()).selectList(any());
    }

    // ==================== 基本匹配 ====================

    @Test
    @DisplayName("用户食材与菜谱食材完全匹配时 matchDegree=100")
    void testApply_ExactMatch_ReturnsPerfectScore() throws Exception {
        // 使用无同义词的食材（牛奶、面粉），扩展后集合大小 = 2
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"));
        Recipe r = recipe(1L, 2001L, ingredientsJson("牛奶", "面粉"), "15", 1, null, null);

        when(recipeMapper.selectList(null)).thenReturn(List.of(r));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("牛奶", "面粉"));
        when(recipeDtoAssembler.convertToListItemDTO(r)).thenReturn(dto(1L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(100, response.getMatchDegree());
        assertFalse(response.getNeedAiGenerate());
        assertEquals(1, response.getRecipes().size());
        assertEquals(100, response.getRecipes().get(0).getMatchDegree());
    }

    @Test
    @DisplayName("部分匹配时 matchDegree 按交集占扩展集的比例计算")
    void testApply_PartialMatch_ReturnsCorrectPercentage() throws Exception {
        // 用户 3 种无同义词食材：牛奶, 面粉, 黄油 → 扩展集大小 = 3
        // 食谱含其中 2 种：牛奶, 面粉 → 交集 = 2
        // matchDegree = 2 * 100 / 3 = 66
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉", "黄油"));
        Recipe r = recipe(1L, 2001L, ingredientsJson("牛奶", "面粉"), "15", 1, null, null);

        when(recipeMapper.selectList(null)).thenReturn(List.of(r));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("牛奶", "面粉"));
        when(recipeDtoAssembler.convertToListItemDTO(r)).thenReturn(dto(1L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(66, response.getMatchDegree());
    }

    @Test
    @DisplayName("完全不匹配时 matchDegree=0")
    void testApply_NoMatch_ReturnsZero() throws Exception {
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶"));
        Recipe r = recipe(1L, 2001L, ingredientsJson("黄油", "芝士"), "15", 1, null, null);

        when(recipeMapper.selectList(null)).thenReturn(List.of(r));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("黄油", "芝士"));
        when(recipeDtoAssembler.convertToListItemDTO(r)).thenReturn(dto(1L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(0, response.getMatchDegree());
        assertEquals(1, response.getRecipes().size());
        assertEquals(0, response.getRecipes().get(0).getMatchDegree());
        assertTrue(response.getNeedAiGenerate());
    }

    // ==================== 同义词匹配 ====================

    @Test
    @DisplayName("同义词扩展后用户食材与菜谱食材匹配成功，分母用原始输入数")
    void testApply_SynonymMatch_DetectedViaIngredientSynonyms() throws Exception {
        // 用户有 "土豆"，原始输入 size=1。扩展为 {土豆, 马铃薯, 洋芋} 用于匹配
        // 食谱有 "马铃薯" → 交集 = {马铃薯}，匹配度 = 1 * 100 / 1 = 100
        ApplyRecipeRequestDTO request = createRequest(List.of("土豆"));
        Recipe r = recipe(1L, 2001L, ingredientsJson("马铃薯"), "15", 1, null, null);

        when(recipeMapper.selectList(null)).thenReturn(List.of(r));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("马铃薯"));
        when(recipeDtoAssembler.convertToListItemDTO(r)).thenReturn(dto(1L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(100, response.getMatchDegree());
    }

    // ==================== 偏好过滤 ====================

    @Test
    @DisplayName("cookTime 超过用户上限的菜谱被跳过")
    void testApply_CookTimeFilter_SkipsRecipe() throws Exception {
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"),
                prefs(20, null, null, null));
        // cookTime = 30 > 20 → 跳过
        Recipe r = recipe(1L, 2001L, ingredientsJson("牛奶", "面粉"), "30", 1, null, null);

        when(recipeMapper.selectList(null)).thenReturn(List.of(r));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("牛奶", "面粉"));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(0, response.getMatchDegree());
        assertTrue(response.getRecipes().isEmpty());
        assertTrue(response.getNeedAiGenerate());
        verify(recipeDtoAssembler, never()).convertToListItemDTO(any());
    }

    @Test
    @DisplayName("difficulty 超过用户上限的菜谱被跳过")
    void testApply_DifficultyFilter_SkipsRecipe() throws Exception {
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"),
                prefs(null, 2, null, null));
        // difficulty = 3 > 2 → 跳过
        Recipe r = recipe(1L, 2001L, ingredientsJson("牛奶", "面粉"), "15", 3, null, null);

        when(recipeMapper.selectList(null)).thenReturn(List.of(r));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("牛奶", "面粉"));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(0, response.getMatchDegree());
        assertTrue(response.getRecipes().isEmpty());
        verify(recipeDtoAssembler, never()).convertToListItemDTO(any());
    }

    @Test
    @DisplayName("cookTime 和 difficulty 均在限制内则菜谱正常纳入")
    void testApply_CookTimeAndDifficultyWithinLimits_IncludesRecipe() throws Exception {
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"),
                prefs(30, 2, null, null));
        Recipe r = recipe(1L, 2001L, ingredientsJson("牛奶", "面粉"), "25", 2, null, null);

        when(recipeMapper.selectList(null)).thenReturn(List.of(r));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("牛奶", "面粉"));
        when(recipeDtoAssembler.convertToListItemDTO(r)).thenReturn(dto(1L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(100, response.getMatchDegree());
        assertEquals(1, response.getRecipes().size());
    }

    // ==================== 口味偏好加分 ====================

    @Test
    @DisplayName("口味偏好匹配时 finalScore = baseMatch×0.7 + tasteScore×0.3")
    void testApply_TastePreferenceBoost_CalculatesCorrectly() throws Exception {
        // baseMatch=100（2/2 完全匹配），tasteHits=1/1（用户偏好 ["辣"]，食谱标签含 "辣"）
        // tasteScore = 100，final = 100×0.7 + 100×0.3 = 100
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"),
                prefs(null, null, List.of("辣"), null));
        Recipe r = recipe(1L, 2001L, ingredientsJson("牛奶", "面粉"), "15", 1,
                "[\"辣\",\"川菜\",\"家常\"]", null);

        when(recipeMapper.selectList(null)).thenReturn(List.of(r));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("牛奶", "面粉"));
        when(objectMapper.readValue(anyString(), eq(String[].class)))
                .thenReturn(new String[]{"辣", "川菜", "家常"});
        when(recipeDtoAssembler.convertToListItemDTO(r)).thenReturn(dto(1L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(100, response.getRecipes().get(0).getMatchDegree());
    }

    @Test
    @DisplayName("口味偏好部分匹配时 finalScore 按比例计算")
    void testApply_TastePreferencePartialMatch_CalculatesCorrectly() throws Exception {
        // baseMatch=50（1/2 匹配），tasteHits=1/2（用户偏好 ["辣","甜"]，食谱仅含 "辣"）
        // tasteScore = 50，final = 50×0.7 + 50×0.3 = 35+15 = 50
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"),
                prefs(null, null, List.of("辣", "甜"), null));
        Recipe r = recipe(1L, 2001L, ingredientsJson("牛奶", "牛肉"), "15", 1,
                "[\"辣\",\"川菜\"]", null);

        when(recipeMapper.selectList(null)).thenReturn(List.of(r));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("牛奶", "牛肉"));
        when(objectMapper.readValue(anyString(), eq(String[].class)))
                .thenReturn(new String[]{"辣", "川菜"});
        when(recipeDtoAssembler.convertToListItemDTO(r)).thenReturn(dto(1L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(50, response.getRecipes().get(0).getMatchDegree());
    }

    // ==================== 饮食偏好加分 ====================

    @Test
    @DisplayName("饮食偏好匹配时 baseMatch+10（无口味加分时）")
    void testApply_DietaryPreferenceBoost_AddsTenPoints() throws Exception {
        // category="lowcal"，dietary=["低卡"] → 匹配 → +10
        // 无口味偏好，score = min(100, baseMatch + 10)
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"),
                prefs(null, null, null, List.of("低卡")));
        Recipe r = recipe(1L, 2001L, ingredientsJson("牛奶", "面粉"), "15", 1,
                null, "lowcal");

        when(recipeMapper.selectList(null)).thenReturn(List.of(r));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("牛奶", "面粉"));
        when(recipeDtoAssembler.convertToListItemDTO(r)).thenReturn(dto(1L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(100, response.getRecipes().get(0).getMatchDegree());
    }

    @Test
    @DisplayName("饮食偏好不匹配时 baseMatch 不变（无口味加分时）")
    void testApply_DietaryPreferenceNoMatch_NoBoost() throws Exception {
        // category="home"，dietary=["低卡"] → 不匹配 → 无加分
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"),
                prefs(null, null, null, List.of("低卡")));
        Recipe r = recipe(1L, 2001L, ingredientsJson("牛奶", "面粉"), "15", 1,
                null, "home");

        when(recipeMapper.selectList(null)).thenReturn(List.of(r));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("牛奶", "面粉"));
        when(recipeDtoAssembler.convertToListItemDTO(r)).thenReturn(dto(1L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(100, response.getRecipes().get(0).getMatchDegree());
    }

    // ==================== 用户档案回退 ====================

    @Test
    @DisplayName("请求无偏好时从用户档案回退口味和饮食偏好")
    void testApply_PreferencesFromUserProfile_FallbackTasteAndDietary() throws Exception {
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"), null);

        // 食谱：标签含 "辣"，分类 "lowcal" → 匹配口味和饮食
        Recipe r = recipe(1L, 2001L, ingredientsJson("牛奶", "面粉"), "15", 1,
                "[\"辣\"]", "lowcal");

        User user = new User();
        user.setId(USER_ID);
        user.setPreferences("{\"taste\":[\"辣\"],\"dietary\":[\"低卡\"]}");

        when(recipeMapper.selectList(null)).thenReturn(List.of(r));
        when(userMapper.selectById(USER_ID)).thenReturn(user);
        when(objectMapper.readValue(anyString(), eq(Map.class)))
                .thenReturn(Map.of("taste", List.of("辣"), "dietary", List.of("低卡")));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("牛奶", "面粉"));
        when(objectMapper.readValue(anyString(), eq(String[].class)))
                .thenReturn(new String[]{"辣"});
        when(recipeDtoAssembler.convertToListItemDTO(r)).thenReturn(dto(1L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        // 口味加分激活（hasTasteBoost=true），饮食加分不再叠加
        assertEquals(100, response.getMatchDegree());
        assertEquals(1, response.getRecipes().size());
    }

    @Test
    @DisplayName("请求有口味偏好但无饮食偏好时只回退饮食偏好")
    void testApply_PreferencesFromUserProfile_FallbackDietaryOnly() throws Exception {
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"),
                prefs(null, null, List.of("辣"), null));
        Recipe r = recipe(1L, 2001L, ingredientsJson("牛奶", "面粉"), "15", 1,
                "[\"辣\"]", "lowcal");

        User user = new User();
        user.setId(USER_ID);
        user.setPreferences("{\"taste\":[\"辣\"],\"dietary\":[\"低卡\"]}");

        when(recipeMapper.selectList(null)).thenReturn(List.of(r));
        when(userMapper.selectById(USER_ID)).thenReturn(user);
        when(objectMapper.readValue(anyString(), eq(Map.class)))
                .thenReturn(Map.of("taste", List.of("辣"), "dietary", List.of("低卡")));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("牛奶", "面粉"));
        when(objectMapper.readValue(anyString(), eq(String[].class)))
                .thenReturn(new String[]{"辣"});
        when(recipeDtoAssembler.convertToListItemDTO(r)).thenReturn(dto(1L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        verify(userMapper).selectById(USER_ID);
        assertEquals(100, response.getMatchDegree());
    }

    @Test
    @DisplayName("请求已提供口味和饮食偏好时 userMapper 不被调用")
    void testApply_PreferencesFromUserProfile_BothProvided_NoFallback() throws Exception {
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"),
                prefs(null, null, List.of("辣"), List.of("低卡")));
        Recipe r = recipe(1L, 2001L, ingredientsJson("牛奶", "面粉"), "15", 1,
                "[\"辣\"]", "lowcal");

        when(recipeMapper.selectList(null)).thenReturn(List.of(r));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("牛奶", "面粉"));
        when(objectMapper.readValue(anyString(), eq(String[].class)))
                .thenReturn(new String[]{"辣"});
        when(recipeDtoAssembler.convertToListItemDTO(r)).thenReturn(dto(1L));

        recipeMatchService.apply(request);

        verify(userMapper, never()).selectById(any());
    }

    @Test
    @DisplayName("用户档案不存在时跳过偏好回退")
    void testApply_PreferencesFromUserProfile_UserNull_NoFallback() throws Exception {
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"), null);

        when(recipeMapper.selectList(null)).thenReturn(List.of());
        when(userMapper.selectById(USER_ID)).thenReturn(null);

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        verify(userMapper).selectById(USER_ID);
        assertEquals(0, response.getMatchDegree());
        assertTrue(response.getRecipes().isEmpty());
    }

    @Test
    @DisplayName("用户档案 preferences 为空字符串时跳过偏好回退")
    void testApply_PreferencesFromUserProfile_EmptyPrefs_NoFallback() throws Exception {
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"), null);

        User user = new User();
        user.setId(USER_ID);
        user.setPreferences("");

        when(recipeMapper.selectList(null)).thenReturn(List.of());
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        verify(userMapper).selectById(USER_ID);
        assertEquals(0, response.getMatchDegree());
    }

    // ==================== 排序 ====================

    @Test
    @DisplayName("多个菜谱按 matchDegree 降序排列")
    void testApply_Sorting_ByMatchDegreeDesc() throws Exception {
        // 用户 4 种无同义词食材 → 扩展集大小=4
        // 食谱 A：全匹配 → 100 分
        // 食谱 B：匹配 2/4 → 50 分
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉", "黄油", "芝士"));
        Recipe a = recipe(1L, 2001L, ingredientsJson("牛奶", "面粉", "黄油", "芝士"), "15", 1, null, null);
        Recipe b = recipe(2L, 2002L, ingredientsJson("牛奶", "面粉"), "15", 1, null, null);

        when(recipeMapper.selectList(null)).thenReturn(List.of(a, b));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(
                        ingredientMaps("牛奶", "面粉", "黄油", "芝士"),
                        ingredientMaps("牛奶", "面粉")
                );
        when(recipeDtoAssembler.convertToListItemDTO(a)).thenReturn(dto(1L));
        when(recipeDtoAssembler.convertToListItemDTO(b)).thenReturn(dto(2L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(2, response.getRecipes().size());
        assertEquals(100, response.getRecipes().get(0).getMatchDegree());
        assertEquals(1L, response.getRecipes().get(0).getId());
        assertEquals(50, response.getRecipes().get(1).getMatchDegree());
        assertEquals(2L, response.getRecipes().get(1).getId());
    }

    @Test
    @DisplayName("相同 matchDegree 时按 cookTime 升序排列")
    void testApply_Sorting_SameMatchDegree_ByCookTimeAsc() throws Exception {
        // 两食谱均完全匹配 → 100 分
        // 食谱 A：cookTime=30，食谱 B：cookTime=15 → B 在前
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"));
        Recipe a = recipe(1L, 2001L, ingredientsJson("牛奶", "面粉"), "30", 1, null, null);
        Recipe b = recipe(2L, 2002L, ingredientsJson("牛奶", "面粉"), "15", 1, null, null);

        when(recipeMapper.selectList(null)).thenReturn(List.of(a, b));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(
                        ingredientMaps("牛奶", "面粉"),
                        ingredientMaps("牛奶", "面粉")
                );
        when(recipeDtoAssembler.convertToListItemDTO(a)).thenReturn(dto(1L));
        when(recipeDtoAssembler.convertToListItemDTO(b)).thenReturn(dto(2L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(2, response.getRecipes().size());
        // cookTime 小的排在前面
        assertEquals(2L, response.getRecipes().get(0).getId());
        assertEquals(1L, response.getRecipes().get(1).getId());
    }

    // ==================== needAiGenerate 逻辑 ====================

    @Test
    @DisplayName("最佳匹配 >=85 时 needAiGenerate=false，返回全部结果")
    void testApply_BestMatchAboveThreshold_NeedAiGenerateFalse() throws Exception {
        // 食谱 1：全匹配 100 分；食谱 2：匹配 1/2 → 50 分
        // bestMatchDegree=100 >= 85 → false，两个都返回
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"));
        Recipe r1 = recipe(1L, 2001L, ingredientsJson("牛奶", "面粉"), "15", 1, null, null);
        Recipe r2 = recipe(2L, 2002L, ingredientsJson("牛奶"), "30", 2, null, null);

        when(recipeMapper.selectList(null)).thenReturn(List.of(r1, r2));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(
                        ingredientMaps("牛奶", "面粉"),
                        ingredientMaps("牛奶")
                );
        when(recipeDtoAssembler.convertToListItemDTO(r1)).thenReturn(dto(1L));
        when(recipeDtoAssembler.convertToListItemDTO(r2)).thenReturn(dto(2L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertFalse(response.getNeedAiGenerate());
        assertEquals(100, response.getMatchDegree());
        assertEquals(2, response.getRecipes().size());
    }

    @Test
    @DisplayName("最佳匹配 <85 时 needAiGenerate=true，只返回前 5 个")
    void testApply_BestMatchBelowThreshold_NeedAiGenerateTrue() throws Exception {
        // 用户 6 种无同义词食材 → 扩展集大小=6
        // 6 个食谱各匹配 1 种食材 → matchDegree = 1*100/6 = 16
        // bestMatchDegree=16 < 85 → true，只返回 5 个
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉", "黄油", "芝士", "奶油", "酵母"));

        // 只创建 5 个食谱，因为 needAiGenerate=true 时 limit(5) 只取前 5 个
        // 若创建 6 个，第 6 个不会被 convertToListItemDTO，导致不必要的 stub 异常
        List<Recipe> recipes = new ArrayList<>();
        for (long i = 1; i <= 5; i++) {
            recipes.add(recipe(i, 2000L + i, ingredientsJson("牛奶"), String.valueOf(10 + i * 5), 1, null, null));
        }

        when(recipeMapper.selectList(null)).thenReturn(recipes);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(ingredientMaps("牛奶"));
        for (Recipe r : recipes) {
            when(recipeDtoAssembler.convertToListItemDTO(r)).thenReturn(dto(r.getId()));
        }

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertTrue(response.getNeedAiGenerate());
        assertEquals(16, response.getMatchDegree());
        assertEquals(5, response.getRecipes().size());
    }

    @Test
    @DisplayName("所有菜谱食材为 null/空白时 matchDegree=0, needAiGenerate=true")
    void testApply_NoMatchedRecipes_AllDefaults() throws Exception {
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"));
        // ingredients 为 null 或空白 → 跳过
        Recipe r1 = recipe(1L, 2001L, null, "15", 1, null, null);
        Recipe r2 = recipe(2L, 2002L, "", "30", 2, null, null);

        when(recipeMapper.selectList(null)).thenReturn(List.of(r1, r2));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(0, response.getMatchDegree());
        assertTrue(response.getRecipes().isEmpty());
        assertTrue(response.getNeedAiGenerate());
        verify(recipeDtoAssembler, never()).convertToListItemDTO(any());
    }

    // ==================== 排除自己的菜谱 ====================

    @Test
    @DisplayName("跳过作者为当前用户的菜谱（authorId == userId）")
    void testApply_OwnRecipe_Skipped() throws Exception {
        ApplyRecipeRequestDTO request = createRequest(List.of("牛奶", "面粉"));
        Recipe own = recipe(1L, USER_ID, ingredientsJson("牛奶", "面粉"), "15", 1, null, null);
        Recipe other = recipe(2L, 2002L, ingredientsJson("牛奶", "面粉"), "15", 1, null, null);

        when(recipeMapper.selectList(null)).thenReturn(List.of(own, other));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(
                        ingredientMaps("牛奶", "面粉"),
                        ingredientMaps("牛奶", "面粉")
                );
        when(recipeDtoAssembler.convertToListItemDTO(other)).thenReturn(dto(2L));

        ApplyRecipeResponseDTO response = recipeMatchService.apply(request);

        assertEquals(1, response.getRecipes().size());
        assertEquals(2L, response.getRecipes().get(0).getId());
    }
}
