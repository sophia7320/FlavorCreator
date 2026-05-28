import java.util.List;

public class Recipe_Request {
    private List<Ingredient> ingredients;
    private Preferences preferences;

    // 内部类：映射 ingredients 数组中的对象
    public static class Ingredient {
        private String name;
        private Integer quantity;
        private String unit;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    // 内部类：映射 preferences 嵌套对象
    public static class Preferences {
        private List<String> taste;
        private List<String> dietary;
        private Integer cookTime;
        private String difficulty;

        public List<String> getTaste() { return taste; }
        public void setTaste(List<String> taste) { this.taste = taste; }
        public List<String> getDietary() { return dietary; }
        public void setDietary(List<String> dietary) { this.dietary = dietary; }
        public Integer getCookTime() { return cookTime; }
        public void setCookTime(Integer cookTime) { this.cookTime = cookTime; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    }

    // 主类的 Getter 和 Setter
    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }
    public Preferences getPreferences() { return preferences; }
    public void setPreferences(Preferences preferences) { this.preferences = preferences; }
}
