package flcr.backend.common.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要 Token 认证的方法
 * required=true（默认）：无有效 Token 直接拒绝
 * required=false：有 Token 则解析，无 Token 也放行
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {

    boolean required() default true;
}