package flcr.backend.common.exception;

import flcr.backend.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Response<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Response.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Response<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Response.error(500, "服务器内部错误");
    }
}