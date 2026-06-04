package flcr.backend.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码加密/验证工具类。
 *
 * <p>BCrypt 自动处理盐值，每次加密结果不同。</p>
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {}

    /**
     * 加密明文密码
     */
    public static String encrypt(String plainPassword) {
        return ENCODER.encode(plainPassword);
    }

    /**
     * 验证明文密码是否匹配存储的 BCrypt 密文。
     */
    public static boolean match(String plainPassword, String storedHash) {
        return ENCODER.matches(plainPassword, storedHash);
    }
}