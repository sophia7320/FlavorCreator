package flcr.backend.common.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Response 统一响应类单元测试
 */
class ResponseTest {

    @Test
    @DisplayName("success() 返回 code=200 message='success' data=null")
    void testSuccessEmpty() {
        Response<Void> response = Response.success();
        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("success(T data) 返回 code=200 并携带 data")
    void testSuccessWithData() {
        Response<String> response = Response.success("hello");
        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("hello", response.getData());
    }

    @Test
    @DisplayName("success(String message, T data) 自定义消息并携带 data")
    void testSuccessWithMessageAndData() {
        Response<Integer> response = Response.success("操作成功", 42);
        assertEquals(200, response.getCode());
        assertEquals("操作成功", response.getMessage());
        assertEquals(42, response.getData());
    }

    @Test
    @DisplayName("error(Integer code, String message) 返回指定错误码和消息")
    void testErrorWithCodeAndMessage() {
        Response<Void> response = Response.error(400, "参数错误");
        assertEquals(400, response.getCode());
        assertEquals("参数错误", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("error(String message) 默认 code=500")
    void testErrorWithMessageOnly() {
        Response<Void> response = Response.error("服务器错误");
        assertEquals(500, response.getCode());
        assertEquals("服务器错误", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Builder 模式构建 Response")
    void testBuilder() {
        Response<String> response = Response.<String>builder()
                .code(201)
                .message("创建成功")
                .data("result")
                .build();
        assertEquals(201, response.getCode());
        assertEquals("创建成功", response.getMessage());
        assertEquals("result", response.getData());
    }

    @Test
    @DisplayName("success 返回的 data 为 null 时 getData 正确")
    void testSuccessNullData() {
        Response<Object> response = Response.success(null);
        assertEquals(200, response.getCode());
        assertNull(response.getData());
    }
}