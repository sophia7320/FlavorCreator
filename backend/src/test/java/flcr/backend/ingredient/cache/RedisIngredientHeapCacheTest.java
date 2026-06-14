package flcr.backend.ingredient.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Redis食材堆缓存测试")
class RedisIngredientHeapCacheTest {

    private static final String EXPIRED_KEY = "ingredient:heap:expired";
    private static final String URGENT_KEY = "ingredient:heap:urgent";
    private static final String WARNING_KEY = "ingredient:heap:warning";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RedisIngredientHeapCache cache;

    private static String dataKey(Long id) {
        return "ingredient:cache:data:" + id;
    }

    @Test
    @DisplayName("push和peekUrgent一致性：push后能从URGENT堆peek回相同数据")
    void testPush_Peek_Consistency() {
        ZSetOperations<String, String> zSetOps = mock(ZSetOperations.class);
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);

        LocalDate expireDate = LocalDate.now().plusDays(2);
        CachedIngredient item = CachedIngredient.builder()
                .id(1L).userId(1001L).name("牛奶")
                .quantity(new BigDecimal("1.5")).unit("升")
                .expireDate(expireDate)
                .readed(false).status(1)
                .build();

        cache.push(item);

        verify(zSetOps).add(eq(URGENT_KEY), eq("1"), anyDouble());
        verify(hashOps).putAll(eq(dataKey(1L)), anyMap());

        when(zSetOps.range(eq(URGENT_KEY), eq(0L), eq(9L))).thenReturn(Set.of("1"));
        Map<Object, Object> hash = new LinkedHashMap<>();
        hash.put("id", "1");
        hash.put("userId", "1001");
        hash.put("name", "牛奶");
        hash.put("expireDate", expireDate.toString());
        hash.put("readed", "false");
        hash.put("status", "1");
        when(hashOps.entries(dataKey(1L))).thenReturn(hash);

        List<CachedIngredient> result = cache.peekUrgent(10);
        assertEquals(1, result.size());
        assertEquals("牛奶", result.get(0).getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("rebuildAll清除旧数据并执行管道批量写入")
    void testRebuildAll_ClearsOldData() {
        CachedIngredient item1 = CachedIngredient.builder()
                .id(1L).userId(1001L).name("鸡蛋")
                .expireDate(LocalDate.now().minusDays(1))
                .build();
        CachedIngredient item2 = CachedIngredient.builder()
                .id(2L).userId(1002L).name("牛奶")
                .expireDate(LocalDate.now().plusDays(2))
                .build();

        cache.rebuildAll(List.of(item1, item2));

        verify(redisTemplate).delete(anySet());
        verify(redisTemplate).executePipelined(any(SessionCallback.class));
    }

    @Test
    @DisplayName("dailyMigrate: URGENT堆中已过期的食材迁移到EXPIRED堆")
    void testDailyMigrate_UrgentToExpired() {
        ZSetOperations<String, String> zSetOps = mock(ZSetOperations.class);
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);

        when(zSetOps.range(eq(URGENT_KEY), eq(0L), eq(0L)))
                .thenReturn(Set.of("1"))
                .thenReturn(Collections.emptySet());

        Map<Object, Object> hash = new LinkedHashMap<>();
        hash.put("id", "1");
        hash.put("userId", "1001");
        hash.put("name", "牛奶");
        hash.put("expireDate", LocalDate.now().minusDays(1).toString());
        hash.put("readed", "false");
        hash.put("status", "1");
        when(hashOps.entries(dataKey(1L))).thenReturn(hash);

        when(zSetOps.range(eq(WARNING_KEY), eq(0L), eq(0L)))
                .thenReturn(Collections.emptySet());

        cache.dailyMigrate();

        verify(zSetOps).remove(eq(URGENT_KEY), eq("1"));
        verify(zSetOps).add(eq(EXPIRED_KEY), eq("1"), anyDouble());
        verify(hashOps).putAll(eq(dataKey(1L)), anyMap());
    }

    @Test
    @DisplayName("dailyMigrate: 堆顶未过期时停止迁移")
    void testDailyMigrate_NoMigration_Stops() {
        ZSetOperations<String, String> zSetOps = mock(ZSetOperations.class);
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);

        when(zSetOps.range(eq(URGENT_KEY), eq(0L), eq(0L)))
                .thenReturn(Set.of("1"));

        Map<Object, Object> hash = new LinkedHashMap<>();
        hash.put("id", "1");
        hash.put("expireDate", LocalDate.now().plusDays(1).toString());
        hash.put("readed", "false");
        hash.put("status", "1");
        when(hashOps.entries(dataKey(1L))).thenReturn(hash);

        when(zSetOps.range(eq(WARNING_KEY), eq(0L), eq(0L)))
                .thenReturn(Collections.emptySet());

        cache.dailyMigrate();

        verify(zSetOps, never()).remove(anyString(), anyString());
        verify(zSetOps, never()).add(anyString(), anyString(), anyDouble());
    }

    @Test
    @DisplayName("cleanupExpired: 已读过期食材从ZSet和Hash中移除")
    void testCleanupExpired_ReadedRemoved() {
        ZSetOperations<String, String> zSetOps = mock(ZSetOperations.class);
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);

        when(zSetOps.range(eq(EXPIRED_KEY), eq(0L), eq(-1L)))
                .thenReturn(Set.of("1"));

        Map<Object, Object> hash = new LinkedHashMap<>();
        hash.put("id", "1");
        hash.put("name", "已过期鸡蛋");
        hash.put("expireDate", LocalDate.now().minusDays(10).toString());
        hash.put("readed", "true");
        hash.put("status", "0");
        when(hashOps.entries(dataKey(1L))).thenReturn(hash);

        cache.cleanupExpired();

        verify(zSetOps).remove(eq(EXPIRED_KEY), eq("1"));
        verify(redisTemplate).delete(dataKey(1L));
    }

    @Test
    @DisplayName("cleanupExpired: 过期超过365天的食材被移除")
    void testCleanupExpired_Over365Days() {
        ZSetOperations<String, String> zSetOps = mock(ZSetOperations.class);
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);

        when(zSetOps.range(eq(EXPIRED_KEY), eq(0L), eq(-1L)))
                .thenReturn(Set.of("1"));

        Map<Object, Object> hash = new LinkedHashMap<>();
        hash.put("id", "1");
        hash.put("name", "极老食材");
        hash.put("expireDate", LocalDate.now().minusDays(400).toString());
        hash.put("readed", "false");
        hash.put("status", "0");
        when(hashOps.entries(dataKey(1L))).thenReturn(hash);

        cache.cleanupExpired();

        verify(zSetOps).remove(eq(EXPIRED_KEY), eq("1"));
        verify(redisTemplate).delete(dataKey(1L));
    }

    @Test
    @DisplayName("hasUnread: EXPIRED堆中有未读食材返回true")
    void testHasUnread_Found() {
        ZSetOperations<String, String> zSetOps = mock(ZSetOperations.class);
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);

        when(zSetOps.range(eq(EXPIRED_KEY), eq(0L), eq(-1L)))
                .thenReturn(new LinkedHashSet<>(List.of("1", "2")));

        when(hashOps.get(dataKey(1L), "readed")).thenReturn("true");
        when(hashOps.get(dataKey(2L), "readed")).thenReturn("false");

        assertTrue(cache.hasUnread());
    }

    @Test
    @DisplayName("markRead后将已读食材标记, hasUnread返回false")
    void testMarkRead_ThenHasUnread() {
        ZSetOperations<String, String> zSetOps = mock(ZSetOperations.class);
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);

        cache.markRead(1L, 1001L, true);
        verify(hashOps).put(dataKey(1L), "readed", "true");

        when(zSetOps.range(eq(EXPIRED_KEY), eq(0L), eq(-1L)))
                .thenReturn(Set.of("1"));
        when(zSetOps.range(eq(URGENT_KEY), eq(0L), eq(-1L)))
                .thenReturn(Set.of("1"));
        when(hashOps.get(dataKey(1L), "readed")).thenReturn("true");

        assertFalse(cache.hasUnread());
    }
}
