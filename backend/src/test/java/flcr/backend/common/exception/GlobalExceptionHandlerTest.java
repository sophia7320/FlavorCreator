package flcr.backend.common.exception;

import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BusinessException返回对应code和message")
    void testHandleBusinessException() {
        BusinessException ex = new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        Response<Void> rsp = handler.handleBusinessException(ex);
        assertEquals(ResultCode.PARAM_ERROR, rsp.getCode());
        assertEquals("参数错误", rsp.getMessage());
    }

    @Test
    @DisplayName("BusinessException权限不足")
    void testHandleBusinessExceptionForbidden() {
        BusinessException ex = new BusinessException(ResultCode.PERMISSION_ERROR, "无权操作");
        Response<Void> rsp = handler.handleBusinessException(ex);
        assertEquals(ResultCode.PERMISSION_ERROR, rsp.getCode());
        assertNotNull(rsp.getMessage());
    }

    @Test
    @DisplayName("Exception返回500系统错误")
    void testHandleException() {
        Exception ex = new RuntimeException("未知错误");
        Response<Void> rsp = handler.handleException(ex);
        assertEquals(ResultCode.SYSTEM_ERROR, rsp.getCode());
    }
}
