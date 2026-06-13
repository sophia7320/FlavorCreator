package flcr.backend.recipe.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DifficultyUtilTest {

    @Nested
    @DisplayName("convertDifficulty 方法")
    class ConvertDifficulty {

        @Test
        @DisplayName("中文输入'简单'返回 1")
        void testConvertDifficulty_SimpleChinese() {
            assertEquals(1, DifficultyUtil.convertDifficulty("简单"));
        }

        @Test
        @DisplayName("中文输入'中等'返回 2")
        void testConvertDifficulty_MediumChinese() {
            assertEquals(2, DifficultyUtil.convertDifficulty("中等"));
        }

        @Test
        @DisplayName("中文输入'困难'返回 3")
        void testConvertDifficulty_HardChinese() {
            assertEquals(3, DifficultyUtil.convertDifficulty("困难"));
        }

        @Test
        @DisplayName("英文输入'simple'返回 1")
        void testConvertDifficulty_SimpleEnglish() {
            assertEquals(1, DifficultyUtil.convertDifficulty("simple"));
        }

        @Test
        @DisplayName("英文输入'medium'返回 2")
        void testConvertDifficulty_MediumEnglish() {
            assertEquals(2, DifficultyUtil.convertDifficulty("medium"));
        }

        @Test
        @DisplayName("英文输入'hard'返回 3")
        void testConvertDifficulty_HardEnglish() {
            assertEquals(3, DifficultyUtil.convertDifficulty("hard"));
        }

        @Test
        @DisplayName("输入 null 返回 null")
        void testConvertDifficulty_Null() {
            assertNull(DifficultyUtil.convertDifficulty(null));
        }

        @Test
        @DisplayName("输入未知字符串返回 null")
        void testConvertDifficulty_Unknown() {
            assertNull(DifficultyUtil.convertDifficulty("unknown"));
        }
    }

    @Nested
    @DisplayName("convertDifficultyToString 方法")
    class ConvertDifficultyToString {

        @Test
        @DisplayName("输入 1 返回'简单'")
        void testConvertDifficultyToString_One() {
            assertEquals("简单", DifficultyUtil.convertDifficultyToString(1));
        }

        @Test
        @DisplayName("输入 2 返回'中等'")
        void testConvertDifficultyToString_Two() {
            assertEquals("中等", DifficultyUtil.convertDifficultyToString(2));
        }

        @Test
        @DisplayName("输入 3 返回'困难'")
        void testConvertDifficultyToString_Three() {
            assertEquals("困难", DifficultyUtil.convertDifficultyToString(3));
        }

        @Test
        @DisplayName("输入 null 返回空字符串")
        void testConvertDifficultyToString_Null() {
            assertEquals("", DifficultyUtil.convertDifficultyToString(null));
        }

        @Test
        @DisplayName("输入未知数字返回'未知'")
        void testConvertDifficultyToString_Unknown() {
            assertEquals("未知", DifficultyUtil.convertDifficultyToString(99));
        }
    }
}
