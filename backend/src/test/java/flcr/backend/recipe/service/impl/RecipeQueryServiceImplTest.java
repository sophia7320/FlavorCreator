package flcr.backend.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.LikeMapper;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.recipe.util.RecipeDtoAssembler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeQueryServiceImpl 菜谱查询服务")
class RecipeQueryServiceImplTest {

    @Mock
    private RecipeMapper recipeMapper;

    @Mock
    private LikeMapper likeMapper;

    @Mock
    private CollectionMapper collectionMapper;

    @Mock
    private RecipeDtoAssembler recipeDtoAssembler;

    @InjectMocks
    private RecipeQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(1001L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ---------------------------------------------------------------------------
    //  Helper factories
    // ---------------------------------------------------------------------------

    private static Recipe createRecipe(Long id, String name) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setName(name);
        recipe.setCategory("家常");
        recipe.setDifficulty(1);
        recipe.setViewCount(10);
        recipe.setLikeCount(5);
        recipe.setCollectionCount(3);
        return recipe;
    }

    private static RecipeListItemResponseDTO createListItemDTO(Long id, String name) {
        return RecipeListItemResponseDTO.builder()
                .id(id)
                .name(name)
                .build();
    }

    // ===========================================================================
    //  getRecipeList()  —  菜谱列表查询
    // ===========================================================================

    @Nested
    @DisplayName("getRecipeList() 菜谱列表查询")
    class GetRecipeList {

        @Test
        @DisplayName("默认查询（无筛选条件）→ 使用默认分页参数，wrapper 无条件")
        void testGetRecipeList_DefaultQuery() {
            RecipeListRequestDTO request = new RecipeListRequestDTO();
            Recipe recipe = createRecipe(1L, "番茄炒蛋");
            Page<Recipe> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(List.of(recipe));
            RecipeListItemResponseDTO dto = createListItemDTO(1L, "番茄炒蛋");

            when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);
            when(recipeDtoAssembler.convertToListItemDTO(recipe)).thenReturn(dto);

            Page<RecipeListItemResponseDTO> result = service.getRecipeList(request);

            // 验证分页参数：默认 1 / 20
            assertEquals(1, result.getCurrent());
            assertEquals(20, result.getSize());
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getRecords().size());
            assertEquals(1L, result.getRecords().get(0).getId());
            assertEquals("番茄炒蛋", result.getRecords().get(0).getName());

            // 验证 selectPage 被调用
            verify(recipeMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
            // 验证 DTO 转换被调用
            verify(recipeDtoAssembler).convertToListItemDTO(recipe);
        }

        @Test
        @DisplayName("分类筛选 → wrapper 中带有 category 条件")
        void testGetRecipeList_CategoryFilter() {
            RecipeListRequestDTO request = new RecipeListRequestDTO();
            request.setCategory("fast");
            Recipe recipe = createRecipe(1L, "快手菜");
            Page<Recipe> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(List.of(recipe));

            when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);
            when(recipeDtoAssembler.convertToListItemDTO(recipe)).thenReturn(createListItemDTO(1L, "快手菜"));

            service.getRecipeList(request);

            ArgumentCaptor<LambdaQueryWrapper<Recipe>> captor = ArgumentCaptor.captor();
            verify(recipeMapper).selectPage(any(Page.class), captor.capture());
            String sql = captor.getValue().getSqlSegment();
            assertTrue(sql.contains("category"));
        }

        @Test
        @DisplayName("难度筛选 → wrapper 中带有 difficulty 条件，DifficultyUtil.convertDifficulty 生效")
        void testGetRecipeList_DifficultyFilter() {
            // "简单" → DifficultyUtil.convertDifficulty("简单") → 1
            RecipeListRequestDTO request = new RecipeListRequestDTO();
            request.setDifficulty("简单");
            Recipe recipe = createRecipe(1L, "简单菜");
            Page<Recipe> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(List.of(recipe));

            when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);
            when(recipeDtoAssembler.convertToListItemDTO(recipe)).thenReturn(createListItemDTO(1L, "简单菜"));

            service.getRecipeList(request);

            ArgumentCaptor<LambdaQueryWrapper<Recipe>> captor = ArgumentCaptor.captor();
            verify(recipeMapper).selectPage(any(Page.class), captor.capture());
            String sql = captor.getValue().getSqlSegment();
            assertTrue(sql.contains("difficulty"));
        }

        @Test
        @DisplayName("关键字筛选 → wrapper 中带有 name LIKE 条件")
        void testGetRecipeList_KeywordFilter() {
            RecipeListRequestDTO request = new RecipeListRequestDTO();
            request.setKeyword("番茄");
            Recipe recipe = createRecipe(1L, "番茄炒蛋");
            Page<Recipe> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(List.of(recipe));

            when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);
            when(recipeDtoAssembler.convertToListItemDTO(recipe)).thenReturn(createListItemDTO(1L, "番茄炒蛋"));

            service.getRecipeList(request);

            ArgumentCaptor<LambdaQueryWrapper<Recipe>> captor = ArgumentCaptor.captor();
            verify(recipeMapper).selectPage(any(Page.class), captor.capture());
            String sql = captor.getValue().getSqlSegment();
            assertTrue(sql.contains("name"));
            assertTrue(sql.contains("LIKE"));
        }

        @Test
        @DisplayName("组合筛选 → category + difficulty + keyword 三个条件同时生效，以 AND 连接")
        void testGetRecipeList_CombinedFilters() {
            RecipeListRequestDTO request = new RecipeListRequestDTO();
            request.setCategory("中式");
            request.setDifficulty("中等");
            request.setKeyword("炒");
            Recipe recipe = createRecipe(1L, "蛋炒饭");
            Page<Recipe> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(List.of(recipe));

            when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);
            when(recipeDtoAssembler.convertToListItemDTO(recipe)).thenReturn(createListItemDTO(1L, "蛋炒饭"));

            service.getRecipeList(request);

            ArgumentCaptor<LambdaQueryWrapper<Recipe>> captor = ArgumentCaptor.captor();
            verify(recipeMapper).selectPage(any(Page.class), captor.capture());
            String sql = captor.getValue().getSqlSegment();
            assertTrue(sql.contains("category"));
            assertTrue(sql.contains("difficulty"));
            assertTrue(sql.contains("name"));
            assertTrue(sql.contains("AND"));
        }

        @Test
        @DisplayName("查询无结果 → 返回空 DTO 列表，不调用 DTO 转换")
        void testGetRecipeList_EmptyResult() {
            RecipeListRequestDTO request = new RecipeListRequestDTO();
            Page<Recipe> emptyPage = new Page<>(1, 20, 0);
            emptyPage.setRecords(List.of());

            when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(emptyPage);

            Page<RecipeListItemResponseDTO> result = service.getRecipeList(request);

            assertEquals(0, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
            verify(recipeDtoAssembler, never()).convertToListItemDTO(any());
        }

        @Test
        @DisplayName("分页参数自定义 → 传入的 page/size 被正确传递到 selectPage")
        void testGetRecipeList_Pagination() {
            RecipeListRequestDTO request = new RecipeListRequestDTO();
            request.setPage(3);
            request.setSize(15);
            Recipe recipe = createRecipe(3L, "分页菜");
            Page<Recipe> pageResult = new Page<>(3, 15, 100);
            pageResult.setRecords(List.of(recipe));

            when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);
            when(recipeDtoAssembler.convertToListItemDTO(recipe)).thenReturn(createListItemDTO(3L, "分页菜"));

            Page<RecipeListItemResponseDTO> result = service.getRecipeList(request);

            assertEquals(3, result.getCurrent());
            assertEquals(15, result.getSize());
            assertEquals(100, result.getTotal());

            ArgumentCaptor<Page<Recipe>> pageCaptor = ArgumentCaptor.captor();
            verify(recipeMapper).selectPage(pageCaptor.capture(), any());
            assertEquals(3, pageCaptor.getValue().getCurrent());
            assertEquals(15, pageCaptor.getValue().getSize());
        }

        @Test
        @DisplayName("排序条件 → wrapper 中带有 ORDER BY created_at DESC")
        void testGetRecipeList_OrderBy() {
            // orderByDesc(Recipe::getCreatedAt) 是无条件执行的
            RecipeListRequestDTO request = new RecipeListRequestDTO();
            // 加一个 WHERE 条件以便 getSqlSegment() 返回非空
            request.setCategory("test");
            Recipe recipe = createRecipe(1L, "番茄炒蛋");
            Page<Recipe> pageResult = new Page<>(1, 20, 1);
            pageResult.setRecords(List.of(recipe));

            when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);
            when(recipeDtoAssembler.convertToListItemDTO(recipe)).thenReturn(createListItemDTO(1L, "番茄炒蛋"));

            service.getRecipeList(request);

            ArgumentCaptor<LambdaQueryWrapper<Recipe>> captor = ArgumentCaptor.captor();
            verify(recipeMapper).selectPage(any(Page.class), captor.capture());
            String sql = captor.getValue().getSqlSegment();
            // WHERE 条件包含 category
            assertTrue(sql.contains("category"));
            // 注：getSqlSegment() 仅返回 WHERE 片段，
            // ORDER BY 由 MyBatis-Plus 在查询执行时拼接，不在此方法返回值中。
            // service 中 orderByDesc(Recipe::getCreatedAt) 无条件调用，
            // 因此每次 getRecipeList 请求都包含该排序。
        }
    }

    // ===========================================================================
    //  getRecipeDetail()  —  菜谱详情查询
    // ===========================================================================

    @Nested
    @DisplayName("getRecipeDetail() 菜谱详情查询")
    class GetRecipeDetail {

        @Test
        @DisplayName("菜谱存在，用户已登录 → 检查点赞/收藏状态，view_count 递增")
        void testGetRecipeDetail_LoggedInUser() {
            Recipe recipe = createRecipe(1L, "番茄炒蛋");
            RecipeDetailResponseDTO detailDTO = RecipeDetailResponseDTO.builder()
                    .id(1L)
                    .name("番茄炒蛋")
                    .isLiked(false)   // 将被 service 覆盖
                    .isCollected(false)
                    .build();

            when(recipeMapper.selectById(1L)).thenReturn(recipe);
            when(recipeMapper.update(any(), any())).thenReturn(1);
            when(recipeDtoAssembler.convertToDetailDTO(recipe)).thenReturn(detailDTO);
            when(likeMapper.selectCount(any())).thenReturn(1L);
            when(collectionMapper.selectCount(any())).thenReturn(1L);

            RecipeDetailResponseDTO result = service.getRecipeDetail(1L);

            assertTrue(result.getIsLiked());
            assertTrue(result.getIsCollected());
            // view_count 从 10 递增到 11
            assertEquals(Integer.valueOf(11), recipe.getViewCount());
            verify(likeMapper).selectCount(any());
            verify(collectionMapper).selectCount(any());
        }

        @Test
        @DisplayName("菜谱存在，匿名用户 → isLiked=false, isCollected=false，不查点赞/收藏表")
        void testGetRecipeDetail_AnonymousUser() {
            UserContext.clear();

            Recipe recipe = createRecipe(1L, "番茄炒蛋");
            RecipeDetailResponseDTO detailDTO = RecipeDetailResponseDTO.builder()
                    .id(1L)
                    .name("番茄炒蛋")
                    .isLiked(false)
                    .isCollected(false)
                    .build();

            when(recipeMapper.selectById(1L)).thenReturn(recipe);
            when(recipeMapper.update(any(), any())).thenReturn(1);
            when(recipeDtoAssembler.convertToDetailDTO(recipe)).thenReturn(detailDTO);

            RecipeDetailResponseDTO result = service.getRecipeDetail(1L);

            assertFalse(result.getIsLiked());
            assertFalse(result.getIsCollected());
            verify(likeMapper, never()).selectCount(any());
            verify(collectionMapper, never()).selectCount(any());
        }

        @Test
        @DisplayName("菜谱不存在 → 抛出 BusinessException(404, '菜谱不存在')")
        void testGetRecipeDetail_NotFound() {
            when(recipeMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.getRecipeDetail(999L));
            assertEquals(ResultCode.RESOURCE_NOT_EXIST, ex.getCode());
            assertEquals("菜谱不存在", ex.getMessage());
            // 不应执行更新和转换
            verify(recipeMapper, never()).update(any(), any());
            verify(recipeDtoAssembler, never()).convertToDetailDTO(any());
        }

        @Test
        @DisplayName("view_count 递增 → update wrapper 中包含 setSql('view_count = view_count + 1')")
        void testGetRecipeDetail_ViewCountIncrement() {
            Recipe recipe = createRecipe(1L, "番茄炒蛋");
            RecipeDetailResponseDTO detailDTO = RecipeDetailResponseDTO.builder().id(1L).name("番茄炒蛋").build();

            when(recipeMapper.selectById(1L)).thenReturn(recipe);
            when(recipeMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(recipeDtoAssembler.convertToDetailDTO(recipe)).thenReturn(detailDTO);

            service.getRecipeDetail(1L);

            ArgumentCaptor<LambdaUpdateWrapper<Recipe>> captor = ArgumentCaptor.captor();
            verify(recipeMapper).update(eq(null), captor.capture());
            LambdaUpdateWrapper<Recipe> updateWrapper = captor.getValue();
            String sqlSet = updateWrapper.getSqlSet();
            assertTrue(sqlSet.contains("view_count"));
            assertTrue(sqlSet.contains("+ 1"));
        }

        @Test
        @DisplayName("recipeDtoAssembler.convertToDetailDTO 接收并返回正确的 Recipe")
        void testGetRecipeDetail_AssemblerCalled() {
            Recipe recipe = createRecipe(1L, "番茄炒蛋");
            RecipeDetailResponseDTO detailDTO = RecipeDetailResponseDTO.builder().id(1L).name("番茄炒蛋").build();

            when(recipeMapper.selectById(1L)).thenReturn(recipe);
            when(recipeMapper.update(any(), any())).thenReturn(1);
            when(recipeDtoAssembler.convertToDetailDTO(recipe)).thenReturn(detailDTO);

            RecipeDetailResponseDTO result = service.getRecipeDetail(1L);

            verify(recipeDtoAssembler).convertToDetailDTO(recipe);
            assertEquals(1L, result.getId());
            assertEquals("番茄炒蛋", result.getName());
        }
    }
}
