package flcr.backend.recipe.util;

import java.util.Map;
import java.util.Set;

public final class IngredientSynonyms {

    private IngredientSynonyms() {}

    private static final Map<String, Set<String>> SYNONYMS = Map.ofEntries(
        Map.entry("土豆", Set.of("马铃薯", "洋芋")),
        Map.entry("马铃薯", Set.of("土豆", "洋芋")),
        Map.entry("番茄", Set.of("西红柿", "蕃茄")),
        Map.entry("西红柿", Set.of("番茄", "蕃茄")),
        Map.entry("鸡蛋", Set.of("鸡子儿", "土鸡蛋")),
        Map.entry("生抽", Set.of("酱油")),
        Map.entry("酱油", Set.of("生抽")),
        Map.entry("老抽", Set.of("酱油")),
        Map.entry("蚝油", Set.of("牡蛎酱")),
        Map.entry("料酒", Set.of("黄酒", "烹饪酒")),
        Map.entry("淀粉", Set.of("生粉", "太白粉")),
        Map.entry("生粉", Set.of("淀粉", "太白粉")),
        Map.entry("鸡胸肉", Set.of("鸡胸", "鸡脯肉")),
        Map.entry("鸡腿", Set.of("鸡腿肉")),
        Map.entry("猪里脊", Set.of("里脊肉", "猪柳")),
        Map.entry("五花肉", Set.of("三层肉", "腩肉")),
        Map.entry("西兰花", Set.of("花椰菜", "青花菜")),
        Map.entry("花菜", Set.of("菜花", "花椰菜")),
        Map.entry("青椒", Set.of("灯笼椒", "柿子椒")),
        Map.entry("洋葱", Set.of("圆葱", "葱头")),
        Map.entry("蒜", Set.of("大蒜", "蒜头")),
        Map.entry("姜", Set.of("生姜", "老姜")),
        Map.entry("葱", Set.of("大葱", "小葱", "香葱")),
        Map.entry("糖", Set.of("白糖", "白砂糖", "砂糖")),
        Map.entry("盐", Set.of("食盐", "精盐")),
        Map.entry("醋", Set.of("陈醋", "白醋", "香醋")),
        Map.entry("辣椒", Set.of("干辣椒", "红辣椒")),
        Map.entry("豆腐", Set.of("嫩豆腐", "老豆腐")),
        Map.entry("虾", Set.of("大虾", "鲜虾", "虾仁")),
        Map.entry("虾仁", Set.of("虾", "大虾")),
        Map.entry("鱼", Set.of("鲜鱼", "整鱼")),
        Map.entry("牛肉", Set.of("牛腩", "牛柳")),
        Map.entry("猪肉", Set.of("猪瘦肉", "猪绞肉")),
        Map.entry("面条", Set.of("挂面", "切面", "面")),
        Map.entry("米饭", Set.of("白米饭", "大米饭")),
        Map.entry("油", Set.of("食用油", "植物油", "菜籽油", "花生油"))
    );

    public static Set<String> getSynonyms(String name) {
        return SYNONYMS.getOrDefault(name, Set.of());
    }

    public static int size() {
        return SYNONYMS.size();
    }
}
