package flcr.backend.recipe.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.common.context.UserContext;
import flcr.backend.recipe.DTO.request.ApplyRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.CreateRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeUpdateRequestDTO;
import flcr.backend.recipe.DTO.response.ApplyRecipeResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeRecommendResponseDTO;
import flcr.backend.recipe.service.RecipeMatchService;
import flcr.backend.recipe.service.RecipeQueryService;
import flcr.backend.recipe.service.RecipeRecommendService;
import flcr.backend.recipe.service.RecipeWriteService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("菜谱服务外观测试")
class RecipeServiceImplTest {

    @Mock private RecipeWriteService recipeWriteService;
    @Mock private RecipeQueryService recipeQueryService;
    @Mock private RecipeMatchService recipeMatchService;
    @Mock private RecipeRecommendService recipeRecommendService;
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
    @DisplayName("publishRecipe委托给RecipeWriteService")
    void testPublishRecipe_Delegates() {
        CreateRecipeRequestDTO request = new CreateRecipeRequestDTO();
        when(recipeWriteService.publishRecipe(request)).thenReturn(1L);
        assertEquals(1L, recipeService.publishRecipe(request));
    }

    @Test
    @DisplayName("updateRecipe委托给RecipeWriteService")
    void testUpdateRecipe_Delegates() {
        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        RecipeDetailResponseDTO detail = RecipeDetailResponseDTO.builder().id(1L).name("test").build();
        when(recipeWriteService.updateRecipe(1L, request)).thenReturn(detail);
        assertEquals(detail, recipeService.updateRecipe(1L, request));
    }

    @Test
    @DisplayName("deleteRecipe委托给RecipeWriteService")
    void testDeleteRecipe_Delegates() {
        doNothing().when(recipeWriteService).deleteRecipe(1L);
        recipeService.deleteRecipe(1L);
        verify(recipeWriteService).deleteRecipe(1L);
    }

    @Test
    @DisplayName("getRecipeList委托给RecipeQueryService")
    void testGetRecipeList_Delegates() {
        RecipeListRequestDTO request = new RecipeListRequestDTO();
        Page<RecipeListItemResponseDTO> page = new Page<>(1, 20);
        when(recipeQueryService.getRecipeList(request)).thenReturn(page);
        assertEquals(page, recipeService.getRecipeList(request));
    }

    @Test
    @DisplayName("getRecipeDetail委托给RecipeQueryService")
    void testGetRecipeDetail_Delegates() {
        RecipeDetailResponseDTO detail = RecipeDetailResponseDTO.builder().id(1L).build();
        when(recipeQueryService.getRecipeDetail(1L)).thenReturn(detail);
        assertEquals(detail, recipeService.getRecipeDetail(1L));
    }

    @Test
    @DisplayName("apply委托给RecipeMatchService")
    void testApply_Delegates() {
        ApplyRecipeRequestDTO request = new ApplyRecipeRequestDTO();
        ApplyRecipeResponseDTO response = ApplyRecipeResponseDTO.builder().matchDegree(100).build();
        when(recipeMatchService.apply(request)).thenReturn(response);
        assertEquals(response, recipeService.apply(request));
    }

    @Test
    @DisplayName("recommend委托给RecipeRecommendService")
    void testRecommend_Delegates() {
        RecipeRecommendResponseDTO response = RecipeRecommendResponseDTO.builder()
                .title("test").recipes(List.of()).build();
        when(recipeRecommendService.recommend()).thenReturn(response);
        assertEquals(response, recipeService.recommend());
    }
}
