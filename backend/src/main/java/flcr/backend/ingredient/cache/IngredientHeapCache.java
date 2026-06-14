package flcr.backend.ingredient.cache;

import java.util.List;

public interface IngredientHeapCache {

    void rebuildAll(List<CachedIngredient> ingredients);

    void push(CachedIngredient ingredient);

    void remove(Long ingredientId, Long userId);

    void update(CachedIngredient ingredient);

    void markRead(Long ingredientId, Long userId, boolean readed);

    void dailyMigrate();

    void cleanupExpired();

    List<CachedIngredient> peekExpired(int limit);

    List<CachedIngredient> peekUrgent(int limit);

    List<CachedIngredient> peekWarning(int limit);

    boolean hasUnread();
}
