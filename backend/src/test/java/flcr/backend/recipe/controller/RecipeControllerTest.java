package flcr.backend.recipe.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.common.response.Response;
import flcr.backend.recipe.DTO.request.PublishRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemDTO;
import flcr.backend.recipe.service.RecipeService;
import flcr.backend.recipe.DTO.request.RecipeGenerateRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeGenerateResponseDTO;
import flcr.backend.recipe.service.RecipeGenerateService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {

    @Mock private RecipeService recipeService;
    @Mock private RecipeGenerateService recipeGenerateService;
    @InjectMocks private RecipeController controller;

    @Test
    @DisplayName("发布菜谱成功返回id")
    void testPublishRecipe() {
        MultipartFile cover = mock(MultipartFile.class);
        List<MultipartFile> images = List.of(mock(MultipartFile.class));
        when(recipeService.publishRecipe(any(), eq(cover), eq(images))).thenReturn(1L);

        assertEquals(1L, controller.publishRecipe(new PublishRecipeRequestDTO(), cover, images).getData());
    }

    @Test
    @DisplayName("发布菜谱无图片")
    void testPublishRecipe_NoImages() {
        MultipartFile cover = mock(MultipartFile.class);
        when(recipeService.publishRecipe(any(), eq(cover), isNull())).thenReturn(2L);

        assertEquals(2L, controller.publishRecipe(new PublishRecipeRequestDTO(), cover, null).getData());
    }

    @Test
    @DisplayName("菜谱列表返回分页")
    void testGetRecipeList() {
        Page<RecipeListItemDTO> page = new Page<>(1, 20);
        when(recipeService.getRecipeList(any())).thenReturn(page);

        assertEquals(200, controller.getRecipeList(new RecipeListRequestDTO()).getCode());
    }

    @Test
    @DisplayName("菜谱详情返回")
    void testGetRecipeDetail() {
        RecipeDetailDTO detail = RecipeDetailDTO.builder().id(1L).name("测试").build();
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
}