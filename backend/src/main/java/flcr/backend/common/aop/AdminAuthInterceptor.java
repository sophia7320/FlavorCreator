package flcr.backend.common.aop;

import flcr.backend.admin.service.impl.AdminAuthServiceImpl;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AdminAuthServiceImpl adminAuthService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);

        if (token == null) {
            log.warn("Admin 未登录访问: {}", request.getRequestURI());
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "未登录，请先登录管理员账号");
        }

        if (!adminAuthService.validateAdminToken(token)) {
            log.warn("Admin Token 无效或已过期: {}", request.getRequestURI());
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "登录已过期，请重新登录");
        }

        Long adminId = adminAuthService.getAdminIdFromToken(token);
        if (adminId == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "登录已过期，请重新登录");
        }

        request.setAttribute("adminId", adminId);
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
