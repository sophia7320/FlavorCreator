package flcr.backend.ingredient.cache;

import flcr.backend.ingredient.constants.IngredientStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Component
@Primary
public class InMemoryIngredientHeapCache implements IngredientHeapCache {

    private static final Logger log = LoggerFactory.getLogger(InMemoryIngredientHeapCache.class);

    private static final Comparator<CachedIngredient> COMPARATOR = Comparator
            .comparing(CachedIngredient::getExpireDate,
                    Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparingLong(CachedIngredient::getId);

    private final PriorityQueue<CachedIngredient> expiredHeap = new PriorityQueue<>(COMPARATOR);
    private final PriorityQueue<CachedIngredient> urgentHeap = new PriorityQueue<>(COMPARATOR);
    private final PriorityQueue<CachedIngredient> warningHeap = new PriorityQueue<>(COMPARATOR);

    private final Object lock = new Object();

    @Override
    public void rebuildAll(List<CachedIngredient> ingredients) {
        synchronized (lock) {
            expiredHeap.clear();
            urgentHeap.clear();
            warningHeap.clear();
            if (ingredients != null) {
                for (CachedIngredient item : ingredients) {
                    IngredientStatus status = IngredientStatus.compute(item.getExpireDate());
                    if (status == IngredientStatus.EXPIRED) {
                        expiredHeap.offer(item);
                    } else if (status == IngredientStatus.URGENT) {
                        urgentHeap.offer(item);
                    } else if (status == IngredientStatus.WARNING) {
                        warningHeap.offer(item);
                    }
                }
            }
        }
    }

    @Override
    public void push(CachedIngredient ingredient) {
        synchronized (lock) {
            IngredientStatus status = IngredientStatus.compute(ingredient.getExpireDate());
            if (status == IngredientStatus.EXPIRED) {
                expiredHeap.offer(ingredient);
            } else if (status == IngredientStatus.URGENT) {
                urgentHeap.offer(ingredient);
            } else if (status == IngredientStatus.WARNING) {
                warningHeap.offer(ingredient);
            }
        }
    }

    @Override
    public void remove(Long ingredientId, Long userId) {
        synchronized (lock) {
            expiredHeap.removeIf(item ->
                    item.getId().equals(ingredientId) && item.getUserId().equals(userId));
            urgentHeap.removeIf(item ->
                    item.getId().equals(ingredientId) && item.getUserId().equals(userId));
            warningHeap.removeIf(item ->
                    item.getId().equals(ingredientId) && item.getUserId().equals(userId));
        }
    }

    @Override
    public void update(CachedIngredient ingredient) {
        synchronized (lock) {
            remove(ingredient.getId(), ingredient.getUserId());
            push(ingredient);
        }
    }

    @Override
    public void markRead(Long ingredientId, Long userId, boolean readed) {
        synchronized (lock) {
            boolean found = false;
            for (CachedIngredient item : expiredHeap) {
                if (item.getId().equals(ingredientId) && item.getUserId().equals(userId)) {
                    item.setReaded(readed);
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (CachedIngredient item : urgentHeap) {
                    if (item.getId().equals(ingredientId) && item.getUserId().equals(userId)) {
                        item.setReaded(readed);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                for (CachedIngredient item : warningHeap) {
                    if (item.getId().equals(ingredientId) && item.getUserId().equals(userId)) {
                        item.setReaded(readed);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public List<CachedIngredient> peekExpired(int limit) {
        synchronized (lock) {
            return peek(expiredHeap, limit);
        }
    }

    @Override
    public List<CachedIngredient> peekUrgent(int limit) {
        synchronized (lock) {
            return peek(urgentHeap, limit);
        }
    }

    @Override
    public List<CachedIngredient> peekWarning(int limit) {
        synchronized (lock) {
            return peek(warningHeap, limit);
        }
    }

    private List<CachedIngredient> peek(PriorityQueue<CachedIngredient> heap, int limit) {
        List<CachedIngredient> result = new ArrayList<>(heap);
        result.sort(COMPARATOR);
        if (result.size() > limit) {
            return new ArrayList<>(result.subList(0, limit));
        }
        return result;
    }

    @Override
    public boolean hasUnread() {
        synchronized (lock) {
            for (CachedIngredient item : urgentHeap) {
                if (item.getReaded() == null || !item.getReaded()) {
                    return true;
                }
            }
            for (CachedIngredient item : expiredHeap) {
                if (item.getReaded() == null || !item.getReaded()) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public void dailyMigrate() {
        synchronized (lock) {
            int migrated = 0;

            // 1. URGENT -> EXPIRED
            while (!urgentHeap.isEmpty()) {
                CachedIngredient top = urgentHeap.peek();
                IngredientStatus status = IngredientStatus.compute(top.getExpireDate());
                if (status == IngredientStatus.EXPIRED) {
                    urgentHeap.poll();
                    top.setReaded(false);
                    expiredHeap.offer(top);
                    migrated++;
                } else {
                    break;
                }
            }

            // 2. WARNING -> URGENT or EXPIRED
            while (!warningHeap.isEmpty()) {
                CachedIngredient top = warningHeap.peek();
                IngredientStatus status = IngredientStatus.compute(top.getExpireDate());
                if (status == IngredientStatus.URGENT) {
                    warningHeap.poll();
                    top.setReaded(false);
                    urgentHeap.offer(top);
                    migrated++;
                } else if (status == IngredientStatus.EXPIRED) {
                    warningHeap.poll();
                    top.setReaded(false);
                    expiredHeap.offer(top);
                    migrated++;
                } else {
                    break;
                }
            }

            // 3. EXPIRED heap: no migration needed (can't get worse)
            if (migrated > 0) {
                log.debug("dailyMigrate: migrated {} items", migrated);
            }
        }
    }

    @Override
    public void cleanupExpired() {
        synchronized (lock) {
            int removed = 0;
            LocalDate now = LocalDate.now();
            var iterator = expiredHeap.iterator();
            while (iterator.hasNext()) {
                CachedIngredient item = iterator.next();
                if (Boolean.TRUE.equals(item.getReaded())) {
                    iterator.remove();
                    removed++;
                } else if (item.getExpireDate() != null
                        && ChronoUnit.DAYS.between(item.getExpireDate(), now) > 365) {
                    iterator.remove();
                    removed++;
                }
            }
            if (removed > 0) {
                log.debug("cleanupExpired: removed {} items", removed);
            }
        }
    }
}
