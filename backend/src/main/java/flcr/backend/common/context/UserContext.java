package flcr.backend.common.context;

/**
 * 用户上下文（ThreadLocal 隔离，请求结束后自动清除）
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> JTI = new ThreadLocal<>();

    private UserContext() {}

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setJti(String jti) {
        JTI.set(jti);
    }

    public static String getJti() {
        return JTI.get();
    }

    public static void clear() {
        USER_ID.remove();
        JTI.remove();
    }
}
