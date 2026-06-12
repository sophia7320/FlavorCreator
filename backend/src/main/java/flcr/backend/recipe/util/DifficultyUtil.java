package flcr.backend.recipe.util;

public final class DifficultyUtil {

    private DifficultyUtil() {}

    public static Integer convertDifficulty(String difficulty) {
        if (difficulty == null) return null;
        switch (difficulty) {
            case "simple":
            case "简单": return 1;
            case "medium":
            case "中等": return 2;
            case "hard":
            case "困难": return 3;
            default: return null;
        }
    }

    public static String convertDifficultyToString(Integer difficulty) {
        if (difficulty == null) return "";
        switch (difficulty) {
            case 1: return "简单";
            case 2: return "中等";
            case 3: return "困难";
            default: return "未知";
        }
    }
}
