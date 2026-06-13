package flcr.backend.recipe.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeValidatorTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RecipeValidator validator;

    @Test
    @DisplayName("validateCategory('fast') 校验通过")
    void testValidateCategory_Fast() {
        assertDoesNotThrow(() -> validator.validateCategory("fast"));
    }

    @Test
    @DisplayName("validateCategory('lowcal') 校验通过")
    void testValidateCategory_Lowcal() {
        assertDoesNotThrow(() -> validator.validateCategory("lowcal"));
    }

    @Test
    @DisplayName("validateCategory('home') 校验通过")
    void testValidateCategory_Home() {
        assertDoesNotThrow(() -> validator.validateCategory("home"));
    }

    @Test
    @DisplayName("validateCategory('special') 校验通过")
    void testValidateCategory_Special() {
        assertDoesNotThrow(() -> validator.validateCategory("special"));
    }

    @Test
    @DisplayName("validateCategory('health') 校验通过")
    void testValidateCategory_Health() {
        assertDoesNotThrow(() -> validator.validateCategory("health"));
    }

    @Test
    @DisplayName("validateCategory('invalid') 抛出异常且消息包含'无效的分类'")
    void testValidateCategory_Invalid() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validateCategory("invalid"));
        assertEquals(ResultCode.PARAM_ERROR, ex.getCode());
        assertTrue(ex.getMessage().contains("无效的分类"));
    }

    @Test
    @DisplayName("validateCategory(null) 抛出 BusinessException")
    void testValidateCategory_Null() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validateCategory(null));
        assertEquals(ResultCode.PARAM_ERROR, ex.getCode());
        assertTrue(ex.getMessage().contains("无效的分类"));
    }

    @Test
    @DisplayName("validateJsonField(null, 'test') 不抛异常")
    void testValidateJsonField_Null() throws Exception {
        assertDoesNotThrow(() -> validator.validateJsonField(null, "test"));
        verify(objectMapper, never()).readTree(anyString());
    }

    @Test
    @DisplayName("validateJsonField(合法JSON, 'test') 调用 readTree 且不抛异常")
    void testValidateJsonField_Valid() throws Exception {
        doReturn(mock(com.fasterxml.jackson.databind.JsonNode.class)).when(objectMapper).readTree(anyString());
        assertDoesNotThrow(() -> validator.validateJsonField("{\"key\":\"value\"}", "test"));
        verify(objectMapper).readTree("{\"key\":\"value\"}");
    }

    @Test
    @DisplayName("validateJsonField(非法JSON, 'test') 抛出异常且消息包含'格式不正确'")
    void testValidateJsonField_Invalid() throws Exception {
        doThrow(new RuntimeException("parse error")).when(objectMapper).readTree(anyString());
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validateJsonField("not-json", "test"));
        assertEquals(ResultCode.PARAM_ERROR, ex.getCode());
        assertTrue(ex.getMessage().contains("格式不正确"));
    }
}
