package flcr.backend.recipe.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IngredientSynonyms 食材同义词工具")
class IngredientSynonymsTest {

    @Nested
    @DisplayName("getSynonyms() 获取食材同义词")
    class GetSynonyms {

        @Test
        @DisplayName("土豆 → 马铃薯、洋芋")
        void test_土豆() {
            assertEquals(Set.of("马铃薯", "洋芋"), IngredientSynonyms.getSynonyms("土豆"));
        }

        @Test
        @DisplayName("马铃薯 → 土豆、洋芋")
        void test_马铃薯() {
            assertEquals(Set.of("土豆", "洋芋"), IngredientSynonyms.getSynonyms("马铃薯"));
        }

        @Test
        @DisplayName("番茄 → 西红柿、蕃茄")
        void test_番茄() {
            assertEquals(Set.of("西红柿", "蕃茄"), IngredientSynonyms.getSynonyms("番茄"));
        }

        @Test
        @DisplayName("西红柿 → 番茄、蕃茄")
        void test_西红柿() {
            assertEquals(Set.of("番茄", "蕃茄"), IngredientSynonyms.getSynonyms("西红柿"));
        }

        @Test
        @DisplayName("鸡蛋 → 鸡子儿、土鸡蛋")
        void test_鸡蛋() {
            assertEquals(Set.of("鸡子儿", "土鸡蛋"), IngredientSynonyms.getSynonyms("鸡蛋"));
        }

        @Test
        @DisplayName("生抽 → 酱油")
        void test_生抽() {
            assertEquals(Set.of("酱油"), IngredientSynonyms.getSynonyms("生抽"));
        }

        @Test
        @DisplayName("酱油 → 生抽")
        void test_酱油() {
            assertEquals(Set.of("生抽"), IngredientSynonyms.getSynonyms("酱油"));
        }

        @Test
        @DisplayName("老抽 → 酱油")
        void test_老抽() {
            assertEquals(Set.of("酱油"), IngredientSynonyms.getSynonyms("老抽"));
        }

        @Test
        @DisplayName("蚝油 → 牡蛎酱")
        void test_蚝油() {
            assertEquals(Set.of("牡蛎酱"), IngredientSynonyms.getSynonyms("蚝油"));
        }

        @Test
        @DisplayName("料酒 → 黄酒、烹饪酒")
        void test_料酒() {
            assertEquals(Set.of("黄酒", "烹饪酒"), IngredientSynonyms.getSynonyms("料酒"));
        }

        @Test
        @DisplayName("淀粉 → 生粉、太白粉")
        void test_淀粉() {
            assertEquals(Set.of("生粉", "太白粉"), IngredientSynonyms.getSynonyms("淀粉"));
        }

        @Test
        @DisplayName("生粉 → 淀粉、太白粉")
        void test_生粉() {
            assertEquals(Set.of("淀粉", "太白粉"), IngredientSynonyms.getSynonyms("生粉"));
        }

        @Test
        @DisplayName("鸡胸肉 → 鸡胸、鸡脯肉")
        void test_鸡胸肉() {
            assertEquals(Set.of("鸡胸", "鸡脯肉"), IngredientSynonyms.getSynonyms("鸡胸肉"));
        }

        @Test
        @DisplayName("鸡腿 → 鸡腿肉")
        void test_鸡腿() {
            assertEquals(Set.of("鸡腿肉"), IngredientSynonyms.getSynonyms("鸡腿"));
        }

        @Test
        @DisplayName("猪里脊 → 里脊肉、猪柳")
        void test_猪里脊() {
            assertEquals(Set.of("里脊肉", "猪柳"), IngredientSynonyms.getSynonyms("猪里脊"));
        }

        @Test
        @DisplayName("五花肉 → 三层肉、腩肉")
        void test_五花肉() {
            assertEquals(Set.of("三层肉", "腩肉"), IngredientSynonyms.getSynonyms("五花肉"));
        }

        @Test
        @DisplayName("西兰花 → 花椰菜、青花菜")
        void test_西兰花() {
            assertEquals(Set.of("花椰菜", "青花菜"), IngredientSynonyms.getSynonyms("西兰花"));
        }

        @Test
        @DisplayName("花菜 → 菜花、花椰菜")
        void test_花菜() {
            assertEquals(Set.of("菜花", "花椰菜"), IngredientSynonyms.getSynonyms("花菜"));
        }

        @Test
        @DisplayName("青椒 → 灯笼椒、柿子椒")
        void test_青椒() {
            assertEquals(Set.of("灯笼椒", "柿子椒"), IngredientSynonyms.getSynonyms("青椒"));
        }

        @Test
        @DisplayName("洋葱 → 圆葱、葱头")
        void test_洋葱() {
            assertEquals(Set.of("圆葱", "葱头"), IngredientSynonyms.getSynonyms("洋葱"));
        }

        @Test
        @DisplayName("蒜 → 大蒜、蒜头")
        void test_蒜() {
            assertEquals(Set.of("大蒜", "蒜头"), IngredientSynonyms.getSynonyms("蒜"));
        }

        @Test
        @DisplayName("姜 → 生姜、老姜")
        void test_姜() {
            assertEquals(Set.of("生姜", "老姜"), IngredientSynonyms.getSynonyms("姜"));
        }

        @Test
        @DisplayName("葱 → 大葱、小葱、香葱")
        void test_葱() {
            assertEquals(Set.of("大葱", "小葱", "香葱"), IngredientSynonyms.getSynonyms("葱"));
        }

        @Test
        @DisplayName("糖 → 白糖、白砂糖、砂糖")
        void test_糖() {
            assertEquals(Set.of("白糖", "白砂糖", "砂糖"), IngredientSynonyms.getSynonyms("糖"));
        }

        @Test
        @DisplayName("盐 → 食盐、精盐")
        void test_盐() {
            assertEquals(Set.of("食盐", "精盐"), IngredientSynonyms.getSynonyms("盐"));
        }

        @Test
        @DisplayName("醋 → 陈醋、白醋、香醋")
        void test_醋() {
            assertEquals(Set.of("陈醋", "白醋", "香醋"), IngredientSynonyms.getSynonyms("醋"));
        }

        @Test
        @DisplayName("辣椒 → 干辣椒、红辣椒")
        void test_辣椒() {
            assertEquals(Set.of("干辣椒", "红辣椒"), IngredientSynonyms.getSynonyms("辣椒"));
        }

        @Test
        @DisplayName("豆腐 → 嫩豆腐、老豆腐")
        void test_豆腐() {
            assertEquals(Set.of("嫩豆腐", "老豆腐"), IngredientSynonyms.getSynonyms("豆腐"));
        }

        @Test
        @DisplayName("虾 → 大虾、鲜虾、虾仁")
        void test_虾() {
            assertEquals(Set.of("大虾", "鲜虾", "虾仁"), IngredientSynonyms.getSynonyms("虾"));
        }

        @Test
        @DisplayName("虾仁 → 虾、大虾")
        void test_虾仁() {
            assertEquals(Set.of("虾", "大虾"), IngredientSynonyms.getSynonyms("虾仁"));
        }

        @Test
        @DisplayName("鱼 → 鲜鱼、整鱼")
        void test_鱼() {
            assertEquals(Set.of("鲜鱼", "整鱼"), IngredientSynonyms.getSynonyms("鱼"));
        }

        @Test
        @DisplayName("牛肉 → 牛腩、牛柳")
        void test_牛肉() {
            assertEquals(Set.of("牛腩", "牛柳"), IngredientSynonyms.getSynonyms("牛肉"));
        }

        @Test
        @DisplayName("猪肉 → 猪瘦肉、猪绞肉")
        void test_猪肉() {
            assertEquals(Set.of("猪瘦肉", "猪绞肉"), IngredientSynonyms.getSynonyms("猪肉"));
        }

        @Test
        @DisplayName("面条 → 挂面、切面、面")
        void test_面条() {
            assertEquals(Set.of("挂面", "切面", "面"), IngredientSynonyms.getSynonyms("面条"));
        }

        @Test
        @DisplayName("米饭 → 白米饭、大米饭")
        void test_米饭() {
            assertEquals(Set.of("白米饭", "大米饭"), IngredientSynonyms.getSynonyms("米饭"));
        }

        @Test
        @DisplayName("油 → 食用油、植物油、菜籽油、花生油")
        void test_油() {
            assertEquals(Set.of("食用油", "植物油", "菜籽油", "花生油"), IngredientSynonyms.getSynonyms("油"));
        }

        @Test
        @DisplayName("不存在的食材 → 返回空集合")
        void test_不存在() {
            Set<String> result = IngredientSynonyms.getSynonyms("不存在");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("null → 抛出 NullPointerException（Map.ofEntries 不支持 null key）")
        void test_null() {
            assertThrows(NullPointerException.class, () -> IngredientSynonyms.getSynonyms(null));
        }
    }

    @Test
    @DisplayName("size() 返回 36 组同义词")
    void testSize() {
        assertEquals(36, IngredientSynonyms.size());
    }
}
