package flcr.backend.recipe.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TasteTagMapper {

    private TasteTagMapper() {}

    private static final Map<String, Set<String>> TASTE_TO_TAGS = Map.ofEntries(
        Map.entry("辣", Set.of("辣", "麻辣", "香辣", "酸辣", "微辣", "川菜", "湘菜", "红油", "水煮")),
        Map.entry("酸", Set.of("酸", "酸辣", "酸甜", "醋溜", "醋", "酸汤")),
        Map.entry("甜", Set.of("甜", "酸甜", "糖醋", "甜品", "蜜汁", "拔丝")),
        Map.entry("清淡", Set.of("清淡", "清炒", "蒸", "白灼", "清蒸", "水煮", "素")),
        Map.entry("鲜", Set.of("鲜", "海鲜", "清蒸", "煲汤", "炖", "上汤")),
        Map.entry("香", Set.of("香", "香煎", "烧烤", "红烧", "干锅", "爆炒", "蒜蓉")),
        Map.entry("咸", Set.of("咸", "下饭", "家常", "酱香", "卤")),
        Map.entry("油", Set.of("油炸", "油煎", "酥", "炸", "干煸"))
    );

    private static final Map<String, String> DIETARY_TO_CATEGORY = Map.of(
        "低卡", "lowcal",
        "快手", "fast",
        "家常", "home",
        "养生", "health"
    );

    public static Set<String> tagsForTaste(String taste) {
        return TASTE_TO_TAGS.getOrDefault(taste, Set.of());
    }

    public static String categoryForDietary(String dietary) {
        return DIETARY_TO_CATEGORY.getOrDefault(dietary, null);
    }

    public static Set<String> getAllTastes() {
        return TASTE_TO_TAGS.keySet();
    }
}
