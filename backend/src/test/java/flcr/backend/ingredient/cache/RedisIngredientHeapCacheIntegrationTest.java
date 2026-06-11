package flcr.backend.ingredient.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DisplayName("Redis食材堆缓存集成测试 (Docker Redis)")
class RedisIngredientHeapCacheIntegrationTest {

    private static RedisIngredientHeapCache cache;
    private static StringRedisTemplate template;

    @BeforeAll
    static void setUp() throws Exception {
        assumeTrue(
                Runtime.getRuntime().exec(new String[]{"docker", "ps"}).waitFor() == 0,
                "Docker 不可用，跳过集成测试"
        );

        Process start = Runtime.getRuntime().exec(new String[]{
                "docker", "run", "-d", "--name", "redis-inttest",
                "-p", "16379:6379", "redis:7-alpine"
        });
        start.waitFor();

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 16379);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();

        template = new StringRedisTemplate(factory);
        template.afterPropertiesSet();

        cache = new RedisIngredientHeapCache(template, new ObjectMapper());
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (template != null) {
            template.getConnectionFactory().getConnection().serverCommands().flushAll();
        }
        Runtime.getRuntime().exec(new String[]{"docker", "stop", "redis-inttest"}).waitFor();
        Runtime.getRuntime().exec(new String[]{"docker", "rm", "redis-inttest"}).waitFor();
    }

    @AfterEach
    void flushRedis() {
        cache.rebuildAll(List.of());
    }

    @Test
    @DisplayName("push→peek: 写入紧急食材后能立即读取")
    void testPush_Peek_Consistency() {
        CachedIngredient item = CachedIngredient.builder()
                .id(1L).userId(1001L).name("牛奶")
                .quantity(new BigDecimal("1.5")).unit("升")
                .expireDate(LocalDate.now().plusDays(2)).readed(false)
                .build();

        cache.push(item);

        List<CachedIngredient> result = cache.peekUrgent(10);
        assertEquals(1, result.size());
        assertEquals("牛奶", result.get(0).getName());
    }

    @Test
    @DisplayName("push NORMAL状态不入堆")
    void testPush_Normal_Skipped() {
        CachedIngredient item = CachedIngredient.builder()
                .id(2L).userId(1001L).name("大米")
                .expireDate(LocalDate.now().plusDays(30)).readed(false)
                .build();

        cache.push(item);

        assertTrue(cache.peekUrgent(10).isEmpty());
        assertTrue(cache.peekWarning(10).isEmpty());
        assertTrue(cache.peekExpired(10).isEmpty());
    }

    @Test
    @DisplayName("rebuildAll: 清空旧数据重建")
    void testRebuildAll() {
        CachedIngredient old = CachedIngredient.builder()
                .id(10L).userId(1001L).name("旧数据")
                .expireDate(LocalDate.now().plusDays(1)).readed(false)
                .build();
        cache.push(old);
        assertEquals(1, cache.peekUrgent(10).size());

        CachedIngredient fresh = CachedIngredient.builder()
                .id(20L).userId(1001L).name("新数据")
                .expireDate(LocalDate.now().plusDays(5)).readed(false)
                .build();
        cache.rebuildAll(List.of(fresh));

        assertTrue(cache.peekUrgent(10).isEmpty());
        List<CachedIngredient> warning = cache.peekWarning(10);
        assertEquals(1, warning.size());
        assertEquals("新数据", warning.get(0).getName());
    }

    @Test
    @DisplayName("update: 更新后状态跟随expireDate变化")
    void testUpdate_StatusChange() {
        CachedIngredient urgent = CachedIngredient.builder()
                .id(3L).userId(1001L).name("鸡蛋")
                .expireDate(LocalDate.now().plusDays(2)).readed(false)
                .build();
        cache.push(urgent);
        assertEquals(1, cache.peekUrgent(10).size());

        CachedIngredient expired = CachedIngredient.builder()
                .id(3L).userId(1001L).name("鸡蛋")
                .expireDate(LocalDate.now().minusDays(1)).readed(false)
                .build();
        cache.update(expired);

        assertTrue(cache.peekUrgent(10).isEmpty());
        assertEquals(1, cache.peekExpired(10).size());
        assertEquals("鸡蛋", cache.peekExpired(10).get(0).getName());
    }

    @Test
    @DisplayName("remove: 从所有堆中删除")
    void testRemove() {
        CachedIngredient item = CachedIngredient.builder()
                .id(4L).userId(1001L).name("青菜")
                .expireDate(LocalDate.now().plusDays(1)).readed(false)
                .build();
        cache.push(item);
        assertEquals(1, cache.peekUrgent(10).size());

        cache.remove(4L, 1001L);

        assertTrue(cache.peekUrgent(10).isEmpty());
    }

    @Test
    @DisplayName("markRead→hasUnread: 标记已读后无未读")
    void testMarkRead_ThenHasUnread() {
        CachedIngredient item = CachedIngredient.builder()
                .id(5L).userId(1001L).name("豆腐")
                .expireDate(LocalDate.now().plusDays(2)).readed(false)
                .build();
        cache.push(item);
        assertTrue(cache.hasUnread());

        cache.markRead(5L, 1001L, true);

        assertFalse(cache.hasUnread());
    }

    @Test
    @DisplayName("dailyMigrate: 迁移后数据不丢失")
    void testDailyMigrate_PreservesWarning() {
        CachedIngredient item = CachedIngredient.builder()
                .id(6L).userId(1001L).name("十天后到期酱油")
                .expireDate(LocalDate.now().plusDays(10)).readed(false)
                .build();
        cache.push(item);
        assertEquals(1, cache.peekWarning(10).size());

        cache.dailyMigrate();

        assertEquals(1, cache.peekWarning(10).size());
        assertEquals("十天后到期酱油", cache.peekWarning(10).get(0).getName());
    }

    @Test
    @DisplayName("cleanupExpired: 已读过期食材被清理")
    void testCleanupExpired_ReadedRemoved() {
        CachedIngredient readAndExpired = CachedIngredient.builder()
                .id(8L).userId(1001L).name("已确认过期蛋")
                .expireDate(LocalDate.now().minusDays(5)).readed(true)
                .build();
        cache.push(readAndExpired);
        assertEquals(1, cache.peekExpired(10).size());

        cache.cleanupExpired();

        assertTrue(cache.peekExpired(10).isEmpty());
    }
}
