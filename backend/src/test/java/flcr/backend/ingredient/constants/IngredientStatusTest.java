package flcr.backend.ingredient.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("食材状态枚举测试")
class IngredientStatusTest {

    @Test
    @DisplayName("compute: null过期日返回NORMAL")
    void testCompute_NullExpireDate() {
        assertEquals(IngredientStatus.NORMAL, IngredientStatus.compute(null));
    }

    @Test
    @DisplayName("compute: 已过期返回EXPIRED")
    void testCompute_Expired() {
        assertEquals(IngredientStatus.EXPIRED, IngredientStatus.compute(LocalDate.now().minusDays(1)));
        assertEquals(IngredientStatus.EXPIRED, IngredientStatus.compute(LocalDate.now().minusDays(30)));
    }

    @Test
    @DisplayName("compute: 距今天0天（今天到期）返回URGENT")
    void testCompute_TodayExpires() {
        assertEquals(IngredientStatus.URGENT, IngredientStatus.compute(LocalDate.now()));
    }

    @Test
    @DisplayName("compute: 距今天1-3天返回URGENT")
    void testCompute_Urgent() {
        assertEquals(IngredientStatus.URGENT, IngredientStatus.compute(LocalDate.now().plusDays(1)));
        assertEquals(IngredientStatus.URGENT, IngredientStatus.compute(LocalDate.now().plusDays(2)));
        assertEquals(IngredientStatus.URGENT, IngredientStatus.compute(LocalDate.now().plusDays(3)));
    }

    @Test
    @DisplayName("compute: 距今天4-15天返回WARNING")
    void testCompute_Warning() {
        assertEquals(IngredientStatus.WARNING, IngredientStatus.compute(LocalDate.now().plusDays(4)));
        assertEquals(IngredientStatus.WARNING, IngredientStatus.compute(LocalDate.now().plusDays(15)));
    }

    @Test
    @DisplayName("compute: 距今天16天以上返回NORMAL")
    void testCompute_Normal() {
        assertEquals(IngredientStatus.NORMAL, IngredientStatus.compute(LocalDate.now().plusDays(16)));
        assertEquals(IngredientStatus.NORMAL, IngredientStatus.compute(LocalDate.now().plusDays(365)));
    }

    @Test
    @DisplayName("getCode: 各状态返回正确code")
    void testGetCode() {
        assertEquals(0, IngredientStatus.EXPIRED.getCode());
        assertEquals(1, IngredientStatus.URGENT.getCode());
        assertEquals(2, IngredientStatus.WARNING.getCode());
        assertEquals(3, IngredientStatus.NORMAL.getCode());
    }

    @Test
    @DisplayName("getLabel: 各状态返回中文标签")
    void testGetLabel() {
        assertEquals("已过期", IngredientStatus.EXPIRED.getLabel());
        assertEquals("红灯", IngredientStatus.URGENT.getLabel());
        assertEquals("黄灯", IngredientStatus.WARNING.getLabel());
        assertEquals("绿灯", IngredientStatus.NORMAL.getLabel());
    }

    @Test
    @DisplayName("fromCode: 有效code返回对应枚举")
    void testFromCode_Valid() {
        assertEquals(IngredientStatus.EXPIRED, IngredientStatus.fromCode(0));
        assertEquals(IngredientStatus.URGENT, IngredientStatus.fromCode(1));
        assertEquals(IngredientStatus.WARNING, IngredientStatus.fromCode(2));
        assertEquals(IngredientStatus.NORMAL, IngredientStatus.fromCode(3));
    }

    @Test
    @DisplayName("fromCode: 无效code返回null")
    void testFromCode_Invalid() {
        assertNull(IngredientStatus.fromCode(-1));
        assertNull(IngredientStatus.fromCode(99));
    }

    @Test
    @DisplayName("isExpired: 仅EXPIRED返回true")
    void testIsExpired() {
        assertTrue(IngredientStatus.EXPIRED.isExpired());
        assertFalse(IngredientStatus.URGENT.isExpired());
        assertFalse(IngredientStatus.WARNING.isExpired());
        assertFalse(IngredientStatus.NORMAL.isExpired());
    }

    @Test
    @DisplayName("isUrgent: 仅URGENT返回true")
    void testIsUrgent() {
        assertFalse(IngredientStatus.EXPIRED.isUrgent());
        assertTrue(IngredientStatus.URGENT.isUrgent());
        assertFalse(IngredientStatus.WARNING.isUrgent());
        assertFalse(IngredientStatus.NORMAL.isUrgent());
    }

    @Test
    @DisplayName("isWarning: 仅WARNING返回true")
    void testIsWarning() {
        assertFalse(IngredientStatus.EXPIRED.isWarning());
        assertFalse(IngredientStatus.URGENT.isWarning());
        assertTrue(IngredientStatus.WARNING.isWarning());
        assertFalse(IngredientStatus.NORMAL.isWarning());
    }

    @Test
    @DisplayName("isNormal: 仅NORMAL返回true")
    void testIsNormal() {
        assertFalse(IngredientStatus.EXPIRED.isNormal());
        assertFalse(IngredientStatus.URGENT.isNormal());
        assertFalse(IngredientStatus.WARNING.isNormal());
        assertTrue(IngredientStatus.NORMAL.isNormal());
    }
}
