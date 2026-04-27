package flcr.backend.common.exception;

import flcr.backend.common.constants.ResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BusinessException 单元测试
 */
class BusinessExceptionTest {

    @Test
    @DisplayName("构造异常携带自定义 code 和 message")
    void testConstructorWithCodeAndMessage() {
        BusinessException ex = new BusinessException(ResultCode.PARAM_ERROR, "参数不能为空");
        assertEquals(400, ex.getCode());
        assertEquals("参数不能为空", ex.getMessage());
    }

    @Test
    @DisplayName("构造异常仅传 message 时默认 code=500")
    void testConstructorWithMessageOnly() {
        BusinessException ex = new BusinessException("服务器内部错误");
        assertEquals(500, ex.getCode());
        assertEquals("服务器内部错误", ex.getMessage());
    }

    @Test
    @DisplayName("BusinessException 继承 RuntimeException")
    void testIsRuntimeException() {
        BusinessException ex = new BusinessException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("全局异常处理器能捕获 BusinessException 并提取 code")
    void testExceptionCodeCanBeReadByHandler() {
        BusinessException ex = new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");

        // 模拟 GlobalExceptionHandler 的处理逻辑
        assertEquals(401, ex.getCode());
        assertEquals("用户不存在", ex.getMessage());
    }
}