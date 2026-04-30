package flcr.backend.common.config;

import flcr.backend.common.aop.AuthInterceptor;
import flcr.backend.common.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(jwtTokenUtil))
                .addPathPatterns("/api/**")
                .order(1);
    }
}
