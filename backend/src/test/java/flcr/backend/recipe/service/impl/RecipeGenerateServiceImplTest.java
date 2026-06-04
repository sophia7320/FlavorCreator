package flcr.backend.recipe.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.recipe.DTO.request.RecipeGenerateRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeGenerateResponseDTO;
import flcr.backend.recipe.client.LlmClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeGenerateServiceImplTest {

    @Mock
    private LlmClient llmClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RecipeGenerateServiceImpl service;

    @Test
    @DisplayName("正常生成菜谱返回 RecipeGenerateResponseDTO")
    void testGenerateRecipe_Success() throws Exception {
        RecipeGenerateRequestDTO request = buildBasicRequest();
        String mockLlmJson = "{\"recipe\":{\"name\":\"番茄炒蛋\"}}";
        RecipeGenerateResponseDTO expected = RecipeGenerateResponseDTO.builder()
                .recipe(RecipeGenerateResponseDTO.RecipeDetail.builder().name("番茄炒蛋").build())
                .build();

        when(llmClient.generateRecipeJson(anyString())).thenReturn(mockLlmJson);
        when(objectMapper.readValue(mockLlmJson, RecipeGenerateResponseDTO.class)).thenReturn(expected);

        RecipeGenerateResponseDTO result = service.generateRecipe(request);

        assertNotNull(result);
        assertEquals("番茄炒蛋", result.getRecipe().getName());
        verify(llmClient).generateRecipeJson(anyString());
    }

    @Test
    @DisplayName("LLM Client 抛 BusinessException 时向上传播")
    void testGenerateRecipe_LlmClientThrowsBusinessException() {
        RecipeGenerateRequestDTO request = buildBasicRequest();

        when(llmClient.generateRecipeJson(anyString()))
                .thenThrow(new BusinessException(ResultCode.SYSTEM_ERROR, "AI服务网络请求失败"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.generateRecipe(request));
        assertEquals(ResultCode.SYSTEM_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("LLM 返回 JSON 解析失败抛 BusinessException")
    void testGenerateRecipe_JsonParseFailure() throws Exception {
        RecipeGenerateRequestDTO request = buildBasicRequest();
        String malformedJson = "not valid json";

        when(llmClient.generateRecipeJson(anyString())).thenReturn(malformedJson);
        when(objectMapper.readValue(malformedJson, RecipeGenerateResponseDTO.class))
                .thenThrow(new JsonProcessingException("parse error") {});

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.generateRecipe(request));
        assertEquals(ResultCode.SYSTEM_ERROR, ex.getCode());
        assertEquals("AI生成格式解析失败", ex.getMessage());
    }

    @Test
    @DisplayName("preferences 为 null 时正常生成")
    void testGenerateRecipe_NullPreferences() throws Exception {
        RecipeGenerateRequestDTO request = new RecipeGenerateRequestDTO();
        RecipeGenerateRequestDTO.Ingredient ing = new RecipeGenerateRequestDTO.Ingredient();
        ing.setName("鸡蛋");
        ing.setQuantity(2);
        ing.setUnit("个");
        request.setIngredients(List.of(ing));
        request.setPreferences(null);

        String mockLlmJson = "{\"recipe\":{\"name\":\"水煮蛋\"}}";
        RecipeGenerateResponseDTO expected = RecipeGenerateResponseDTO.builder()
                .recipe(RecipeGenerateResponseDTO.RecipeDetail.builder().name("水煮蛋").build())
                .build();

        when(llmClient.generateRecipeJson(anyString())).thenReturn(mockLlmJson);
        when(objectMapper.readValue(mockLlmJson, RecipeGenerateResponseDTO.class)).thenReturn(expected);

        RecipeGenerateResponseDTO result = service.generateRecipe(request);

        assertNotNull(result);
        assertEquals("水煮蛋", result.getRecipe().getName());
    }

    @Test
    @DisplayName("ingredient quantity 为 null 时默认 0")
    void testGenerateRecipe_NullQuantity() throws Exception {
        RecipeGenerateRequestDTO request = new RecipeGenerateRequestDTO();
        RecipeGenerateRequestDTO.Ingredient ing = new RecipeGenerateRequestDTO.Ingredient();
        ing.setName("鸡蛋");
        ing.setQuantity(null);
        ing.setUnit("个");
        request.setIngredients(List.of(ing));

        String mockLlmJson = "{\"recipe\":{\"name\":\"鸡蛋料理\"}}";
        RecipeGenerateResponseDTO expected = RecipeGenerateResponseDTO.builder()
                .recipe(RecipeGenerateResponseDTO.RecipeDetail.builder().name("鸡蛋料理").build())
                .build();

        when(llmClient.generateRecipeJson(anyString())).thenReturn(mockLlmJson);
        when(objectMapper.readValue(mockLlmJson, RecipeGenerateResponseDTO.class)).thenReturn(expected);

        RecipeGenerateResponseDTO result = service.generateRecipe(request);

        assertNotNull(result);
        verify(llmClient).generateRecipeJson(contains("鸡蛋: 0 个"));
    }

    @Test
    @DisplayName("preferences 中列表字段为 null 时显示'无'")
    void testGenerateRecipe_NullTasteAndDietary() throws Exception {
        RecipeGenerateRequestDTO request = new RecipeGenerateRequestDTO();
        RecipeGenerateRequestDTO.Ingredient ing = new RecipeGenerateRequestDTO.Ingredient();
        ing.setName("鸡蛋");
        ing.setQuantity(2);
        ing.setUnit("个");
        request.setIngredients(List.of(ing));

        RecipeGenerateRequestDTO.Preferences pref = new RecipeGenerateRequestDTO.Preferences();
        pref.setTaste(null);
        pref.setDietary(null);
        pref.setCookTime(null);
        pref.setDifficulty(null);
        request.setPreferences(pref);

        String mockLlmJson = "{\"recipe\":{\"name\":\"鸡蛋\"}}";
        RecipeGenerateResponseDTO expected = RecipeGenerateResponseDTO.builder()
                .recipe(RecipeGenerateResponseDTO.RecipeDetail.builder().name("鸡蛋").build())
                .build();

        when(llmClient.generateRecipeJson(anyString())).thenReturn(mockLlmJson);
        when(objectMapper.readValue(mockLlmJson, RecipeGenerateResponseDTO.class)).thenReturn(expected);

        RecipeGenerateResponseDTO result = service.generateRecipe(request);

        assertNotNull(result);
        verify(llmClient).generateRecipeJson(argThat(prompt ->
                prompt.contains("口味: 无") && prompt.contains("饮食限制: 无")));
    }

    @Test
    @DisplayName("LLM 返回含 markdown 标记的 JSON 正常解析")
    void testGenerateRecipe_StripMarkdownFences() throws Exception {
        RecipeGenerateRequestDTO request = buildBasicRequest();
        String llmJsonWithFences = "```json\n{\"recipe\":{\"name\":\"青椒炒蛋\"}}\n```";
        String cleanedJson = "{\"recipe\":{\"name\":\"青椒炒蛋\"}}";
        RecipeGenerateResponseDTO expected = RecipeGenerateResponseDTO.builder()
                .recipe(RecipeGenerateResponseDTO.RecipeDetail.builder().name("青椒炒蛋").build())
                .build();

        when(llmClient.generateRecipeJson(anyString())).thenReturn(llmJsonWithFences);
        when(objectMapper.readValue(cleanedJson, RecipeGenerateResponseDTO.class)).thenReturn(expected);

        RecipeGenerateResponseDTO result = service.generateRecipe(request);

        assertNotNull(result);
        assertEquals("青椒炒蛋", result.getRecipe().getName());
    }

    private RecipeGenerateRequestDTO buildBasicRequest() {
        RecipeGenerateRequestDTO request = new RecipeGenerateRequestDTO();

        RecipeGenerateRequestDTO.Ingredient ing1 = new RecipeGenerateRequestDTO.Ingredient();
        ing1.setName("鸡蛋");
        ing1.setQuantity(3);
        ing1.setUnit("个");
        RecipeGenerateRequestDTO.Ingredient ing2 = new RecipeGenerateRequestDTO.Ingredient();
        ing2.setName("番茄");
        ing2.setQuantity(2);
        ing2.setUnit("个");
        request.setIngredients(List.of(ing1, ing2));

        RecipeGenerateRequestDTO.Preferences pref = new RecipeGenerateRequestDTO.Preferences();
        pref.setTaste(List.of("清淡"));
        pref.setDietary(List.of("低卡"));
        pref.setCookTime(30);
        pref.setDifficulty("简单");
        request.setPreferences(pref);

        return request;
    }
}
