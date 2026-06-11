package flcr.backend.ingredient.cache;

import flcr.backend.ingredient.constants.IngredientStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("内存食材堆缓存测试")
class InMemoryIngredientHeapCacheTest {

    private InMemoryIngredientHeapCache cache;

    @BeforeEach
    void setUp() {
        cache = new InMemoryIngredientHeapCache();
    }

    @Test
    @DisplayName("push: 过期食材进入expired堆")
    void testPush_Expired() {
        CachedIngredient item = CachedIngredient.builder()
                .id(1L).userId(1001L).name("鸡蛋")
                .expireDate(LocalDate.now().minusDays(1))
                .readed(false).status(0)
                .build();

        cache.push(item);

        List<CachedIngredient> result = cache.peekExpired(10);
        assertEquals(1, result.size());
        assertEquals("鸡蛋", result.get(0).getName());
    }

    @Test
    @DisplayName("push: 临期食材(≤3天)进入urgent堆")
    void testPush_Urgent() {
        CachedIngredient item = CachedIngredient.builder()
                .id(2L).userId(1001L).name("牛奶")
                .expireDate(LocalDate.now().plusDays(2))
                .readed(false).status(1)
                .build();

        cache.push(item);

        List<CachedIngredient> urgent = cache.peekUrgent(10);
        assertEquals(1, urgent.size());
        assertEquals("牛奶", urgent.get(0).getName());

        assertTrue(cache.peekExpired(10).isEmpty());
        assertTrue(cache.peekWarning(10).isEmpty());
    }

    @Test
    @DisplayName("push: 黄灯食材(4-15天)进入warning堆")
    void testPush_Warning() {
        CachedIngredient item = CachedIngredient.builder()
                .id(3L).userId(1001L).name("番茄")
                .expireDate(LocalDate.now().plusDays(10))
                .readed(false).status(2)
                .build();

        cache.push(item);

        List<CachedIngredient> warning = cache.peekWarning(10);
        assertEquals(1, warning.size());
        assertEquals("番茄", warning.get(0).getName());

        assertTrue(cache.peekUrgent(10).isEmpty());
        assertTrue(cache.peekExpired(10).isEmpty());
    }

    @Test
    @DisplayName("push: 正常食材(>15天)不进入任何堆")
    void testPush_Normal() {
        CachedIngredient item = CachedIngredient.builder()
                .id(4L).userId(1001L).name("面粉")
                .expireDate(LocalDate.now().plusDays(30))
                .readed(false).status(3)
                .build();

        cache.push(item);

        assertTrue(cache.peekExpired(10).isEmpty());
        assertTrue(cache.peekUrgent(10).isEmpty());
        assertTrue(cache.peekWarning(10).isEmpty());
    }

    @Test
    @DisplayName("push: 堆内按expireDate升序排列")
    void testPush_SortedByExpireDate() {
        CachedIngredient later = CachedIngredient.builder()
                .id(10L).userId(1001L).name("晚到期")
                .expireDate(LocalDate.now().plusDays(5))
                .readed(false).status(2)
                .build();
        CachedIngredient sooner = CachedIngredient.builder()
                .id(20L).userId(1001L).name("早到期")
                .expireDate(LocalDate.now().plusDays(1))
                .readed(false).status(1)
                .build();

        cache.push(later);
        cache.push(sooner);

        List<CachedIngredient> urgent = cache.peekUrgent(10);
        assertEquals(1, urgent.size());
        assertEquals("早到期", urgent.get(0).getName());
    }

    @Test
    @DisplayName("peekWarning: limit截断只返回指定数量")
    void testPeek_LimitTruncation() {
        for (int i = 0; i < 5; i++) {
            cache.push(CachedIngredient.builder()
                    .id((long) i).userId(1001L).name("食材" + i)
                    .expireDate(LocalDate.now().plusDays(10 + i))
                    .readed(false).status(2)
                    .build());
        }

        List<CachedIngredient> result = cache.peekWarning(3);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("dailyMigrate: URGENT变EXPIRED时迁移到过期堆并重置readed")
    void testDailyMigrate_UrgentToExpired() {
        CachedIngredient urgent = CachedIngredient.builder()
                .id(1L).userId(1001L).name("即将过期的蛋")
                .expireDate(LocalDate.now().plusDays(1))
                .readed(true).status(1)
                .build();
        cache.push(urgent);

        urgent.setExpireDate(LocalDate.now().minusDays(1));

        cache.dailyMigrate();

        assertEquals(0, cache.peekUrgent(10).size());
        List<CachedIngredient> expired = cache.peekExpired(10);
        assertEquals(1, expired.size());
        assertEquals("即将过期的蛋", expired.get(0).getName());
        assertFalse(expired.get(0).getReaded());
    }

    @Test
    @DisplayName("dailyMigrate: WARNING变URGENT时迁移并重置readed")
    void testDailyMigrate_WarningToUrgent() {
        CachedIngredient warning = CachedIngredient.builder()
                .id(2L).userId(1002L).name("即将临期的菜")
                .expireDate(LocalDate.now().plusDays(10))
                .readed(true).status(2)
                .build();
        cache.push(warning);

        warning.setExpireDate(LocalDate.now().plusDays(2));

        cache.dailyMigrate();

        assertEquals(0, cache.peekWarning(10).size());
        List<CachedIngredient> urgent = cache.peekUrgent(10);
        assertEquals(1, urgent.size());
        assertFalse(urgent.get(0).getReaded());
    }

    @Test
    @DisplayName("dailyMigrate: 堆顶状态不变时停止迁移")
    void testDailyMigrate_StopsWhenNoMigration() {
        cache.push(CachedIngredient.builder()
                .id(3L).userId(1003L).name("正常食材")
                .expireDate(LocalDate.now().plusDays(30))
                .readed(false).status(3)
                .build());

        assertDoesNotThrow(() -> cache.dailyMigrate());
    }

    @Test
    @DisplayName("cleanupExpired: readed=true的过期食材被移除")
    void testCleanupExpired_RemovesRead() {
        CachedIngredient read = CachedIngredient.builder()
                .id(1L).userId(1001L).name("已确认")
                .expireDate(LocalDate.now().minusDays(10))
                .readed(true).status(0)
                .build();
        CachedIngredient unread = CachedIngredient.builder()
                .id(2L).userId(1001L).name("未确认")
                .expireDate(LocalDate.now().minusDays(5))
                .readed(false).status(0)
                .build();
        cache.push(read);
        cache.push(unread);

        cache.cleanupExpired();

        List<CachedIngredient> expired = cache.peekExpired(10);
        assertEquals(1, expired.size());
        assertEquals("未确认", expired.get(0).getName());
    }

    @Test
    @DisplayName("cleanupExpired: 过期超过365天的被移除")
    void testCleanupExpired_RemovesOld() {
        CachedIngredient veryOld = CachedIngredient.builder()
                .id(1L).userId(1001L).name("极老食材")
                .expireDate(LocalDate.now().minusDays(400))
                .readed(false).status(0)
                .build();
        cache.push(veryOld);

        cache.cleanupExpired();

        assertTrue(cache.peekExpired(10).isEmpty());
    }

    @Test
    @DisplayName("hasUnread: urgent堆有未读食材返回true")
    void testHasUnread_True() {
        CachedIngredient unread = CachedIngredient.builder()
                .id(1L).userId(1001L).name("未读")
                .expireDate(LocalDate.now().plusDays(1))
                .readed(false).status(1)
                .build();
        cache.push(unread);

        assertTrue(cache.hasUnread());
    }

    @Test
    @DisplayName("hasUnread: 全部已读返回false")
    void testHasUnread_False() {
        CachedIngredient read = CachedIngredient.builder()
                .id(1L).userId(1001L).name("已读")
                .expireDate(LocalDate.now().plusDays(1))
                .readed(true).status(1)
                .build();
        cache.push(read);

        assertFalse(cache.hasUnread());
    }
}
