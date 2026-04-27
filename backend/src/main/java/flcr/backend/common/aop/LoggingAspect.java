package flcr.backend.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.StringJoiner;

/**
 * 面向切片日志记录
 * 记录 Controller 和 Service 层方法调用的入参、出参和执行耗时
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * 切点：所有 Controller 和 Service 的 public 方法
     */
    @Pointcut("execution(public * flcr.backend..controller..*(..)) || execution(public * flcr.backend..service..*(..))")
    public void logPointcut() {}

    @Around("logPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        // 构建入参日志
        StringJoiner paramsJoiner = new StringJoiner(", ");
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                paramsJoiner.add(paramNames[i] + "=" + formatArg(args[i]));
            }
        }

        log.info("→ {}.{}({})", className, methodName, paramsJoiner);

        StopWatch sw = new StopWatch();
        sw.start();
        try {
            Object result = joinPoint.proceed();
            sw.stop();
            log.info("← {}.{}() 耗时={}ms 返回={}",
                    className, methodName, sw.getTotalTimeMillis(), formatArg(result));
            return result;
        } catch (Exception e) {
            sw.stop();
            log.error("✗ {}.{}() 耗时={}ms 异常={}",
                    className, methodName, sw.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }

    /**
     * 格式化参数，避免长字符串撑爆日志
     */
    private String formatArg(Object arg) {
        if (arg == null) {
            return "null";
        }
        String str = arg.toString();
        if (str.length() > 200) {
            return str.substring(0, 200) + "...";
        }
        return str;
    }
}