package flcr.backend.common.config;

import flcr.backend.admin.service.impl.AdminAuthServiceImpl;
import flcr.backend.common.aop.AdminAuthInterceptor;
import flcr.backend.common.aop.AuthInterceptor;
import flcr.backend.common.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtTokenUtil jwtTokenUtil;

    @Autowired(required = false)
    private AdminAuthServiceImpl adminAuthService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(jwtTokenUtil))
                .addPathPatterns("/api/**")
                .order(1);

        if (adminAuthService != null) {
            registry.addInterceptor(new AdminAuthInterceptor(adminAuthService))
                    .addPathPatterns("/api/admin/**")
                    .excludePathPatterns("/api/admin/auth/login")
                    .excludePathPatterns("/api/admin/auth/refresh")
                    .order(0);
        }
    }
}
