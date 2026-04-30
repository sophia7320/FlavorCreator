package flcr.backend.common.aop;

import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        UserContext.clear();

        boolean isPublic = hm.hasMethodAnnotation(Public.class);
        String token = extractToken(request);

        if (isPublic) {
            if (token != null) {
                Long userId = jwtTokenUtil.getUserIdFromToken(token);
                if (userId != null) {
                    UserContext.setUserId(userId);
                }
            }
            return true;
        }

        if (token == null) {
            log.warn("未登录访问需要认证的接口: {}", request.getRequestURI());
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "未登录，请先授权");
        }

        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("Token 无效或已过期: {}", request.getRequestURI());
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "登录已过期，请重新登录");
        }

        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
