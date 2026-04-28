package flcr.backend.common.service.impl;

import flcr.backend.common.util.JwtTokenUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private TokenBlacklistServiceImpl tokenBlacklistService;

    @Test
    @DisplayName("有效token写入Redis黑名单")
    void testBlacklist_ValidToken() {
        String jti = "test-jti-123";
        when(jwtTokenUtil.getRemainingTime(jti)).thenReturn(5000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        tokenBlacklistService.blacklist(jti);

        verify(valueOps).set(eq("token:blacklist:" + jti), eq("1"), eq(5000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("已过期token不写入Redis")
    void testBlacklist_ExpiredToken() {
        String jti = "expired-jti";
        when(jwtTokenUtil.getRemainingTime(jti)).thenReturn(0L);

        tokenBlacklistService.blacklist(jti);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("黑名单中存在返回true")
    void testIsBlacklisted_True() {
        String jti = "jti-in-blacklist";
        when(redisTemplate.hasKey("token:blacklist:" + jti)).thenReturn(true);

        assertTrue(tokenBlacklistService.isBlacklisted(jti));
    }

    @Test
    @DisplayName("黑名单中不存在返回false")
    void testIsBlacklisted_False() {
        String jti = "jti-not-in-blacklist";
        when(redisTemplate.hasKey("token:blacklist:" + jti)).thenReturn(false);

        assertFalse(tokenBlacklistService.isBlacklisted(jti));
    }

    @Test
    @DisplayName("Redis异常时返回false（可用性优先）")
    void testIsBlacklisted_RedisException() {
        String jti = "any-jti";
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis down"));

        assertFalse(tokenBlacklistService.isBlacklisted(jti));
    }
}
