package flcr.backend.common.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.common.service.RefreshTokenService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    @Mock private org.springframework.data.redis.core.ValueOperations<String, String> valueOperations;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private RefreshTokenServiceImpl refreshTokenService;

    private static final Long USER_ID = 1001L;
    private static final String OPENID = "test_openid";
    private static final String REFRESH_TOKEN = "test-uuid-refresh-token";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        refreshTokenService = new RefreshTokenServiceImpl(redisTemplate, objectMapper);
    }

    @Test
    @DisplayName("store 将数据写入 Redis，TTL 为 30 天")
    void testStore() {
        refreshTokenService.store(USER_ID, OPENID, REFRESH_TOKEN);

        verify(valueOperations).set(
                eq("rt:" + REFRESH_TOKEN),
                anyString(),
                eq(2592000L),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    @DisplayName("get 存在时返回数据对象")
    void testGet_Success() throws Exception {
        String json = objectMapper.writeValueAsString(
                new RefreshTokenService.RefreshTokenData(USER_ID, OPENID));
        when(valueOperations.get("rt:" + REFRESH_TOKEN)).thenReturn(json);

        RefreshTokenService.RefreshTokenData result = refreshTokenService.get(REFRESH_TOKEN);

        assertNotNull(result);
        assertEquals(USER_ID, result.userId());
        assertEquals(OPENID, result.openid());
    }

    @Test
    @DisplayName("get 不存在时返回 null")
    void testGet_NotFound() {
        when(valueOperations.get("rt:" + REFRESH_TOKEN)).thenReturn(null);

        RefreshTokenService.RefreshTokenData result = refreshTokenService.get(REFRESH_TOKEN);

        assertNull(result);
    }

    @Test
    @DisplayName("delete 删除 Redis key")
    void testDelete() {
        refreshTokenService.delete(REFRESH_TOKEN);

        verify(redisTemplate).delete("rt:" + REFRESH_TOKEN);
    }
}
