package flcr.backend.common.aop;

import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.service.TokenBlacklistService;
import flcr.backend.common.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class AuthAspect {

    private final JwtTokenUtil jwtTokenUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Around("@annotation(requireAuth)")
    public Object authenticate(ProceedingJoinPoint joinPoint, RequireAuth requireAuth) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        String token = extractToken(request);

        if (requireAuth.required() && token == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "未登录，请先授权");
        }

        if (token != null) {
            if (!jwtTokenUtil.validateToken(token)) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST, "登录已过期，请重新登录");
            }
            String jti = jwtTokenUtil.getJtiFromToken(token);
            if (tokenBlacklistService.isBlacklisted(jti)) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST, "Token 已失效，请重新登录");
            }
            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST, "Token 无效");
            }
            UserContext.setUserId(userId);
            UserContext.setJti(jti);
        }

        try {
            return joinPoint.proceed();
        } finally {
            UserContext.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest();
    }
}
