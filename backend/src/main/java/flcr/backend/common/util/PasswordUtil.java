package flcr.backend.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码加密/验证工具类
 *
 * <p>用于 Admin 模块密码加密存储和登录验证。</p>
 * <p>BCrypt 自动处理盐值，每次加密结果不同，</p>
 * <p>match 方法兼容明文密码（用于存量数据迁移过渡期）。</p>
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
     * 验证明文密码是否匹配存储的密文
     * <p>兼容明文存储的存量密码（当密文不是BCrypt格式时回退到明文比较）。</p>
     */
    public static boolean match(String plainPassword, String storedHash) {
        // 如果是BCrypt格式，用BCrypt验证
        if (isBcrypt(storedHash)) {
            return ENCODER.matches(plainPassword, storedHash);
        }
        // 兼容存量明文密码（迁移过渡期）
        return plainPassword.equals(storedHash);
    }

    /**
     * 判断是否为 BCrypt 格式的哈希值
     * BCrypt hash 以 $2a$、$2b$ 或 $2y$ 开头
     */
    private static boolean isBcrypt(String hash) {
        return hash != null && hash.length() == 60
                && (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
    }
}
