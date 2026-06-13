package flcr.backend.recipe.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TasteTagMapper 口味标签映射工具")
class TasteTagMapperTest {

    @Nested
    @DisplayName("tagsForTaste() 口味 → 标签集合")
    class TagsForTaste {

        @Test
        @DisplayName("辣 → 正确标签集合")
        void testTagsForTaste_辣() {
            Set<String> tags = TasteTagMapper.tagsForTaste("辣");
            assertEquals(Set.of("辣", "麻辣", "香辣", "酸辣", "微辣", "川菜", "湘菜", "红油", "水煮"), tags);
        }

        @Test
        @DisplayName("酸 → 正确标签集合")
        void testTagsForTaste_酸() {
            Set<String> tags = TasteTagMapper.tagsForTaste("酸");
            assertEquals(Set.of("酸", "酸辣", "酸甜", "醋溜", "醋", "酸汤"), tags);
        }

        @Test
        @DisplayName("甜 → 正确标签集合")
        void testTagsForTaste_甜() {
            Set<String> tags = TasteTagMapper.tagsForTaste("甜");
            assertEquals(Set.of("甜", "酸甜", "糖醋", "甜品", "蜜汁", "拔丝"), tags);
        }

        @Test
        @DisplayName("清淡 → 正确标签集合")
        void testTagsForTaste_清淡() {
            Set<String> tags = TasteTagMapper.tagsForTaste("清淡");
            assertEquals(Set.of("清淡", "清炒", "蒸", "白灼", "清蒸", "水煮", "素"), tags);
        }

        @Test
        @DisplayName("鲜 → 正确标签集合")
        void testTagsForTaste_鲜() {
            Set<String> tags = TasteTagMapper.tagsForTaste("鲜");
            assertEquals(Set.of("鲜", "海鲜", "清蒸", "煲汤", "炖", "上汤"), tags);
        }

        @Test
        @DisplayName("香 → 正确标签集合")
        void testTagsForTaste_香() {
            Set<String> tags = TasteTagMapper.tagsForTaste("香");
            assertEquals(Set.of("香", "香煎", "烧烤", "红烧", "干锅", "爆炒", "蒜蓉"), tags);
        }

        @Test
        @DisplayName("咸 → 正确标签集合")
        void testTagsForTaste_咸() {
            Set<String> tags = TasteTagMapper.tagsForTaste("咸");
            assertEquals(Set.of("咸", "下饭", "家常", "酱香", "卤"), tags);
        }

        @Test
        @DisplayName("油 → 正确标签集合")
        void testTagsForTaste_油() {
            Set<String> tags = TasteTagMapper.tagsForTaste("油");
            assertEquals(Set.of("油炸", "油煎", "酥", "炸", "干煸"), tags);
        }

        @Test
        @DisplayName("未知口味 → 返回空集合")
        void testTagsForTaste_未知() {
            Set<String> tags = TasteTagMapper.tagsForTaste("未知");
            assertTrue(tags.isEmpty());
        }

        @Test
        @DisplayName("null → 返回空集合")
        void testTagsForTaste_null() {
            Set<String> result = TasteTagMapper.tagsForTaste(null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("categoryForDietary() 饮食偏好 → 分类标识")
    class CategoryForDietary {

        @Test
        @DisplayName("低卡 → lowcal")
        void testCategoryForDietary_低卡() {
            assertEquals("lowcal", TasteTagMapper.categoryForDietary("低卡"));
        }

        @Test
        @DisplayName("快手 → fast")
        void testCategoryForDietary_快手() {
            assertEquals("fast", TasteTagMapper.categoryForDietary("快手"));
        }

        @Test
        @DisplayName("家常 → home")
        void testCategoryForDietary_家常() {
            assertEquals("home", TasteTagMapper.categoryForDietary("家常"));
        }

        @Test
        @DisplayName("养生 → health")
        void testCategoryForDietary_养生() {
            assertEquals("health", TasteTagMapper.categoryForDietary("养生"));
        }

        @Test
        @DisplayName("未知饮食偏好 → 返回 null")
        void testCategoryForDietary_未知() {
            assertNull(TasteTagMapper.categoryForDietary("未知"));
        }

        @Test
        @DisplayName("null → 返回 null")
        void testCategoryForDietary_null() {
            assertNull(TasteTagMapper.categoryForDietary(null));
        }
    }

    @Nested
    @DisplayName("getAllTastes() 所有口味键")
    class GetAllTastes {

        @Test
        @DisplayName("返回 8 种口味键")
        void testGetAllTastes() {
            Set<String> tastes = TasteTagMapper.getAllTastes();
            assertEquals(Set.of("辣", "酸", "甜", "清淡", "鲜", "香", "咸", "油"), tastes);
        }

        @Test
        @DisplayName("返回不可变集合")
        void testGetAllTastes_immutable() {
            Set<String> tastes = TasteTagMapper.getAllTastes();
            assertThrows(UnsupportedOperationException.class, () -> tastes.add("新口味"));
        }
    }
}
