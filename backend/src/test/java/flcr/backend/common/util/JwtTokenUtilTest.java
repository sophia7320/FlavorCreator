package flcr.backend.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;

    private static final String TEST_SECRET = "TestSecretKey";
    private static final Long TEST_USER_ID = 100L;
    private static final String TEST_OPENID = "testOpenid123";

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", 7200000L);
        ReflectionTestUtils.setField(jwtTokenUtil, "refreshExpiration", 604800000L);
    }

    @Test
    @DisplayName("生成访问令牌不应为空")
    void testGenerateToken() {
        String token = jwtTokenUtil.generateToken(TEST_USER_ID, TEST_OPENID);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("验证有效令牌返回 true")
    void testValidateToken_Success() {
        String token = jwtTokenUtil.generateToken(TEST_USER_ID, TEST_OPENID);
        assertTrue(jwtTokenUtil.validateToken(token));
    }

    @Test
    @DisplayName("验证无效令牌返回 false")
    void testValidateToken_InvalidToken() {
        assertFalse(jwtTokenUtil.validateToken("invalidToken"));
    }

    @Test
    @DisplayName("验证空令牌返回 false")
    void testValidateToken_NullOrEmpty() {
        assertFalse(jwtTokenUtil.validateToken(null));
        assertFalse(jwtTokenUtil.validateToken(""));
    }

    @Test
    @DisplayName("从令牌中获取用户 ID")
    void testGetUserIdFromToken() {
        String token = jwtTokenUtil.generateToken(TEST_USER_ID, TEST_OPENID);
        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        assertEquals(TEST_USER_ID, userId);
    }

    @Test
    @DisplayName("从无效令牌中获取用户 ID 返回 null")
    void testGetUserIdFromToken_Invalid() {
        assertNull(jwtTokenUtil.getUserIdFromToken("invalid"));
    }

    @Test
    @DisplayName("从令牌中获取 OpenID")
    void testGetOpenidFromToken() {
        String token = jwtTokenUtil.generateToken(TEST_USER_ID, TEST_OPENID);
        String openid = jwtTokenUtil.getOpenidFromToken(token);
        assertEquals(TEST_OPENID, openid);
    }

    @Test
    @DisplayName("从无效令牌中获取 OpenID 返回 null")
    void testGetOpenidFromToken_Invalid() {
        assertNull(jwtTokenUtil.getOpenidFromToken("invalid"));
    }

    @Test
    @DisplayName("使用不同密钥生成的令牌验证失败")
    void testValidateToken_WrongSecret() {
        JwtTokenUtil otherUtil = new JwtTokenUtil();
        ReflectionTestUtils.setField(otherUtil, "secret", "DifferentSecret");
        ReflectionTestUtils.setField(otherUtil, "expiration", 7200000L);
        ReflectionTestUtils.setField(otherUtil, "refreshExpiration", 604800000L);

        String token = otherUtil.generateToken(TEST_USER_ID, TEST_OPENID);
        assertFalse(jwtTokenUtil.validateToken(token));
    }
}
