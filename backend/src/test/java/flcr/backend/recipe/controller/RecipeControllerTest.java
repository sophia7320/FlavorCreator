package flcr.backend.recipe.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.common.response.Response;
import flcr.backend.recipe.DTO.request.PublishRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.service.RecipeService;
import flcr.backend.recipe.DTO.request.RecipeGenerateRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeGenerateResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeRecommendResponseDTO;
import flcr.backend.recipe.service.RecipeGenerateService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("菜谱控制器测试")
class RecipeControllerTest {

    @Mock private RecipeService recipeService;
    @Mock private RecipeGenerateService recipeGenerateService;
    @InjectMocks private RecipeController controller;

    @Test
    @DisplayName("发布菜谱成功返回id")
    void testPublishRecipe_ReturnsId() {
        PublishRecipeRequestDTO dto = new PublishRecipeRequestDTO();
        dto.setCoverUrl("https://example.com/cover.jpg");
        dto.setImageUrls(List.of("https://example.com/img1.jpg"));
        when(recipeService.publishRecipe(any())).thenReturn(1L);

        assertEquals(1L, controller.publishRecipe(dto).getData());
    }

    @Test
    @DisplayName("发布菜谱无图片")
    void testPublishRecipe_NoImages() {
        PublishRecipeRequestDTO dto = new PublishRecipeRequestDTO();
        when(recipeService.publishRecipe(any())).thenReturn(2L);

        assertEquals(2L, controller.publishRecipe(dto).getData());
    }

    @Test
    @DisplayName("菜谱列表返回分页")
    void testGetRecipeList_ReturnsPage() {
        Page<RecipeListItemResponseDTO> page = new Page<>(1, 20);
        when(recipeService.getRecipeList(any())).thenReturn(page);

        assertEquals(200, controller.getRecipeList(new RecipeListRequestDTO()).getCode());
    }

    @Test
    @DisplayName("菜谱详情返回")
    void testGetRecipeDetail_ReturnsDetail() {
        RecipeDetailResponseDTO detail = RecipeDetailResponseDTO.builder().id(1L).name("测试").build();
        when(recipeService.getRecipeDetail(1L)).thenReturn(detail);

        assertEquals("测试", controller.getRecipeDetail(1L).getData().getName());
    }

    @Test
    @DisplayName("AI菜谱生成成功返回 RecipeGenerateResponseDTO")
    void testGenerateRecipe_Success() {
        RecipeGenerateRequestDTO request = new RecipeGenerateRequestDTO();
        RecipeGenerateResponseDTO expected = RecipeGenerateResponseDTO.builder()
                .recipe(RecipeGenerateResponseDTO.RecipeDetail.builder().name("番茄炒蛋").build())
                .build();
        when(recipeGenerateService.generateRecipe(any())).thenReturn(expected);

        Response<RecipeGenerateResponseDTO> response = controller.generateRecipe(request);

        assertEquals(200, response.getCode());
        assertEquals("番茄炒蛋", response.getData().getRecipe().getName());
    }

    @Test
    @DisplayName("今日推荐返回200")
    void testRecommend_ReturnsOk() {
        RecipeRecommendResponseDTO dto = RecipeRecommendResponseDTO.builder()
                .title("今天吃什么？")
                .recipes(List.of(
                        RecipeRecommendResponseDTO.RecommendItem.builder()
                                .id(1L).name("测试菜").cover("https://...").reason("推荐").build()))
                .build();
        when(recipeService.recommend()).thenReturn(dto);
        Response<RecipeRecommendResponseDTO> response = controller.recommend();
        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().getRecipes().size());
    }
}