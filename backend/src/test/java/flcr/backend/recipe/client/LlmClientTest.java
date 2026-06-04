package flcr.backend.recipe.client;

import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LlmClientTest {

    private static final String API_URL = "https://test.api.example.com/v1/chat";
    private static final String MODEL_NAME = "test-model";
    private static final String API_KEY = "Bearer test-key";

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LlmClient llmClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(llmClient, "apiUrl", API_URL);
        ReflectionTestUtils.setField(llmClient, "modelName", MODEL_NAME);
        ReflectionTestUtils.setField(llmClient, "apiKey", API_KEY);
    }

    @Test
    @DisplayName("LLM API 调用成功返回 content")
    void testGenerateRecipeJson_Success() {
        Map<String, Object> message = Map.of("content", "{\"recipe\":{\"name\":\"测试菜\"}}");
        Map<String, Object> choice = Map.of("message", message);
        Map<String, Object> body = Map.of("choices", List.of(choice));

        ResponseEntity<Map<String, Object>> responseEntity = ResponseEntity.ok(body);

        when(restTemplate.postForEntity(eq(API_URL), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);

        String result = llmClient.generateRecipeJson("test prompt");

        assertNotNull(result);
        assertEquals("{\"recipe\":{\"name\":\"测试菜\"}}", result);
    }

    @Test
    @DisplayName("LLM API 返回非200状态抛 BusinessException")
    void testGenerateRecipeJson_NonOkStatus() {
        ResponseEntity<Map<String, Object>> responseEntity =
                new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.postForEntity(eq(API_URL), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> llmClient.generateRecipeJson("test prompt"));
        assertEquals(ResultCode.SYSTEM_ERROR, ex.getCode());
        assertEquals("AI服务响应异常", ex.getMessage());
    }

    @Test
    @DisplayName("LLM API 返回空 body 抛 BusinessException")
    void testGenerateRecipeJson_NullBody() {
        ResponseEntity<Map<String, Object>> responseEntity =
                new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.postForEntity(eq(API_URL), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> llmClient.generateRecipeJson("test prompt"));
        assertEquals(ResultCode.SYSTEM_ERROR, ex.getCode());
        assertEquals("AI服务响应异常", ex.getMessage());
    }

    @Test
    @DisplayName("LLM API choices 为空抛 BusinessException")
    void testGenerateRecipeJson_EmptyChoices() {
        Map<String, Object> body = Map.of("choices", List.of());

        ResponseEntity<Map<String, Object>> responseEntity = ResponseEntity.ok(body);

        when(restTemplate.postForEntity(eq(API_URL), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> llmClient.generateRecipeJson("test prompt"));
        assertEquals(ResultCode.SYSTEM_ERROR, ex.getCode());
        assertEquals("AI服务返回为空", ex.getMessage());
    }

    @Test
    @DisplayName("LLM API choices 中 message 为 null 抛 BusinessException")
    void testGenerateRecipeJson_NullMessage() {
        Map<String, Object> choice = new java.util.HashMap<>();
        choice.put("message", null);
        Map<String, Object> body = Map.of("choices", List.of(choice));

        ResponseEntity<Map<String, Object>> responseEntity = ResponseEntity.ok(body);

        when(restTemplate.postForEntity(eq(API_URL), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> llmClient.generateRecipeJson("test prompt"));
        assertEquals(ResultCode.SYSTEM_ERROR, ex.getCode());
        assertEquals("AI服务返回为空", ex.getMessage());
    }

    @Test
    @DisplayName("LLM API 网络异常抛 BusinessException")
    void testGenerateRecipeJson_NetworkError() {
        when(restTemplate.postForEntity(eq(API_URL), any(HttpEntity.class), any(Class.class)))
                .thenThrow(new RestClientException("Connection refused"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> llmClient.generateRecipeJson("test prompt"));
        assertEquals(ResultCode.SYSTEM_ERROR, ex.getCode());
        assertEquals("AI服务网络请求失败", ex.getMessage());
    }
}
