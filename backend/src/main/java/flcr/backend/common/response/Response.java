package flcr.backend.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果类
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 成功响应
     */
    public static <T> Response<T> success() {
        return Response.<T>builder()
                .code(200)
                .message("success")
                .build();
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Response<T> success(T data) {
        return Response.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    /**
     * 成功响应（带消息和数据）
     */
    public static <T> Response<T> success(String message, T data) {
        return Response.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> Response<T> error(Integer code, String message) {
        return Response.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * 失败响应（默认错误码 500）
     */
    public static <T> Response<T> error(String message) {
        return Response.<T>builder()
                .code(500)
                .message(message)
                .build();
    }
}
