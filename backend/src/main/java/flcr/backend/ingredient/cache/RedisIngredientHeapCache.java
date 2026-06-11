package flcr.backend.ingredient.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.ingredient.constants.IngredientStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "flcr.cache.type", havingValue = "redis")
@RequiredArgsConstructor
public class RedisIngredientHeapCache implements IngredientHeapCache {

    private static final String EXPIRED_KEY = "ingredient:heap:expired";
    private static final String URGENT_KEY = "ingredient:heap:urgent";
    private static final String WARNING_KEY = "ingredient:heap:warning";
    private static final String DATA_PREFIX = "ingredient:cache:data:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static String dataKey(Long ingredientId) {
        return DATA_PREFIX + ingredientId;
    }

    @Override
    public void rebuildAll(List<CachedIngredient> ingredients) {
        Set<String> keys = new HashSet<>();
        keys.add(EXPIRED_KEY);
        keys.add(URGENT_KEY);
        keys.add(WARNING_KEY);
        for (CachedIngredient ci : ingredients) {
            keys.add(dataKey(ci.getId()));
        }
        redisTemplate.delete(keys);

        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) {
                for (CachedIngredient ci : ingredients) {
                    IngredientStatus status = IngredientStatus.compute(ci.getExpireDate());
                    if (status != IngredientStatus.NORMAL) {
                        String heapKey = getHeapKey(status);
                        double score = ci.getExpireDate() != null
                                ? ci.getExpireDate().toEpochDay()
                                : Long.MAX_VALUE;
                        operations.opsForZSet().add(heapKey, String.valueOf(ci.getId()), score);
                        operations.opsForHash().putAll(dataKey(ci.getId()), toHash(ci));
                    }
                }
                return null;
            }
        });
    }

    @Override
    public void push(CachedIngredient ingredient) {
        IngredientStatus status = IngredientStatus.compute(ingredient.getExpireDate());
        if (status == IngredientStatus.NORMAL) {
            return;
        }
        String heapKey = getHeapKey(status);
        double score = ingredient.getExpireDate() != null
                ? ingredient.getExpireDate().toEpochDay()
                : Long.MAX_VALUE;
        redisTemplate.opsForZSet().add(heapKey, String.valueOf(ingredient.getId()), score);
        redisTemplate.opsForHash().putAll(dataKey(ingredient.getId()), toHash(ingredient));
    }

    @Override
    public void remove(Long ingredientId, Long userId) {
        String idStr = String.valueOf(ingredientId);
        redisTemplate.opsForZSet().remove(EXPIRED_KEY, idStr);
        redisTemplate.opsForZSet().remove(URGENT_KEY, idStr);
        redisTemplate.opsForZSet().remove(WARNING_KEY, idStr);
        redisTemplate.delete(dataKey(ingredientId));
    }

    @Override
    public void update(CachedIngredient ingredient) {
        remove(ingredient.getId(), ingredient.getUserId());
        push(ingredient);
    }

    @Override
    public void markRead(Long ingredientId, Long userId, boolean readed) {
        redisTemplate.opsForHash().put(dataKey(ingredientId), "readed", String.valueOf(readed));
    }

    @Override
    public List<CachedIngredient> peekExpired(int limit) {
        return peekHeap(EXPIRED_KEY, limit);
    }

    @Override
    public List<CachedIngredient> peekUrgent(int limit) {
        return peekHeap(URGENT_KEY, limit);
    }

    @Override
    public List<CachedIngredient> peekWarning(int limit) {
        return peekHeap(WARNING_KEY, limit);
    }

    @Override
    public boolean hasUnread() {
        Set<String> expiredIds = redisTemplate.opsForZSet().range(EXPIRED_KEY, 0, -1);
        if (hasUnreadIn(expiredIds)) {
            return true;
        }
        Set<String> urgentIds = redisTemplate.opsForZSet().range(URGENT_KEY, 0, -1);
        return hasUnreadIn(urgentIds);
    }

    private boolean hasUnreadIn(Set<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (String id : ids) {
            Object readed = redisTemplate.opsForHash().get(
                    dataKey(Long.parseLong(id)), "readed");
            if (!"true".equals(readed)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void dailyMigrate() {
        // 1. Migrate EXPIRED from URGENT heap
        while (true) {
            Set<String> top = redisTemplate.opsForZSet().range(URGENT_KEY, 0, 0);
            if (top == null || top.isEmpty()) {
                break;
            }
            String idStr = top.iterator().next();
            Map<Object, Object> hash = redisTemplate.opsForHash().entries(
                    dataKey(Long.parseLong(idStr)));
            if (hash.isEmpty()) {
                redisTemplate.opsForZSet().remove(URGENT_KEY, idStr);
                continue;
            }
            CachedIngredient ci = fromHash(hash);
            if (IngredientStatus.compute(ci.getExpireDate()) == IngredientStatus.EXPIRED) {
                redisTemplate.opsForZSet().remove(URGENT_KEY, idStr);
                double score = ci.getExpireDate() != null
                        ? ci.getExpireDate().toEpochDay()
                        : Long.MAX_VALUE;
                redisTemplate.opsForZSet().add(EXPIRED_KEY, idStr, score);
                hash.put("status", String.valueOf(IngredientStatus.EXPIRED.getCode()));
                hash.put("readed", "false");
                redisTemplate.opsForHash().putAll(dataKey(Long.parseLong(idStr)), hash);
            } else {
                break;
            }
        }

        // 2. Migrate from WARNING heap
        while (true) {
            Set<String> top = redisTemplate.opsForZSet().range(WARNING_KEY, 0, 0);
            if (top == null || top.isEmpty()) {
                break;
            }
            String idStr = top.iterator().next();
            Map<Object, Object> hash = redisTemplate.opsForHash().entries(
                    dataKey(Long.parseLong(idStr)));
            if (hash.isEmpty()) {
                redisTemplate.opsForZSet().remove(WARNING_KEY, idStr);
                continue;
            }
            CachedIngredient ci = fromHash(hash);
            IngredientStatus status = IngredientStatus.compute(ci.getExpireDate());
            if (status == IngredientStatus.URGENT) {
                redisTemplate.opsForZSet().remove(WARNING_KEY, idStr);
                double score = ci.getExpireDate() != null
                        ? ci.getExpireDate().toEpochDay()
                        : Long.MAX_VALUE;
                redisTemplate.opsForZSet().add(URGENT_KEY, idStr, score);
                hash.put("status", String.valueOf(IngredientStatus.URGENT.getCode()));
                hash.put("readed", "false");
                redisTemplate.opsForHash().putAll(dataKey(Long.parseLong(idStr)), hash);
            } else if (status == IngredientStatus.EXPIRED) {
                redisTemplate.opsForZSet().remove(WARNING_KEY, idStr);
                double score = ci.getExpireDate() != null
                        ? ci.getExpireDate().toEpochDay()
                        : Long.MAX_VALUE;
                redisTemplate.opsForZSet().add(EXPIRED_KEY, idStr, score);
                hash.put("status", String.valueOf(IngredientStatus.EXPIRED.getCode()));
                hash.put("readed", "false");
                redisTemplate.opsForHash().putAll(dataKey(Long.parseLong(idStr)), hash);
            } else {
                break;
            }
        }
    }

    @Override
    public void cleanupExpired() {
        Set<String> ids = redisTemplate.opsForZSet().range(EXPIRED_KEY, 0, -1);
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (String idStr : ids) {
            Long ingredientId = Long.parseLong(idStr);
            Map<Object, Object> hash = redisTemplate.opsForHash().entries(dataKey(ingredientId));
            if (hash.isEmpty()) {
                redisTemplate.opsForZSet().remove(EXPIRED_KEY, idStr);
                continue;
            }
            CachedIngredient ci = fromHash(hash);
            boolean readed = ci.getReaded() != null && ci.getReaded();
            if (readed) {
                redisTemplate.opsForZSet().remove(EXPIRED_KEY, idStr);
                redisTemplate.delete(dataKey(ingredientId));
                continue;
            }
            if (ci.getExpireDate() != null) {
                long daysSinceExpiry = ChronoUnit.DAYS.between(ci.getExpireDate(), LocalDate.now());
                if (daysSinceExpiry > 365) {
                    redisTemplate.opsForZSet().remove(EXPIRED_KEY, idStr);
                    redisTemplate.delete(dataKey(ingredientId));
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    private String getHeapKey(IngredientStatus status) {
        switch (status) {
            case EXPIRED: return EXPIRED_KEY;
            case URGENT:  return URGENT_KEY;
            case WARNING: return WARNING_KEY;
            default: throw new IllegalArgumentException("Unexpected status: " + status);
        }
    }

    private List<CachedIngredient> peekHeap(String heapKey, int limit) {
        Set<String> ids = redisTemplate.opsForZSet().range(heapKey, 0, limit - 1);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<CachedIngredient> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            Map<Object, Object> hash = redisTemplate.opsForHash().entries(
                    dataKey(Long.parseLong(id)));
            if (!hash.isEmpty()) {
                result.add(fromHash(hash));
            }
        }
        return result;
    }

    private Map<String, String> toHash(CachedIngredient ci) {
        Map<String, String> hash = new LinkedHashMap<>();
        hash.put("id", String.valueOf(ci.getId()));
        hash.put("userId", String.valueOf(ci.getUserId()));
        hash.put("name", ci.getName() != null ? ci.getName() : "");
        hash.put("quantity", ci.getQuantity() != null ? ci.getQuantity().toString() : "");
        hash.put("unit", ci.getUnit() != null ? ci.getUnit() : "");
        hash.put("category", ci.getCategory() != null ? ci.getCategory() : "");
        hash.put("storageCondition", ci.getStorageCondition() != null ? ci.getStorageCondition() : "");
        hash.put("expireDate", ci.getExpireDate() != null ? ci.getExpireDate().toString() : "");
        hash.put("createdAt", ci.getCreatedAt() != null ? ci.getCreatedAt().toString() : "");
        hash.put("readed", ci.getReaded() != null ? String.valueOf(ci.getReaded()) : "false");
        hash.put("status", ci.getStatus() != null ? String.valueOf(ci.getStatus()) : "");
        return hash;
    }

    private CachedIngredient fromHash(Map<Object, Object> hash) {
        CachedIngredient ci = new CachedIngredient();
        Object val;

        val = hash.get("id");
        if (val != null) ci.setId(Long.parseLong(val.toString()));

        val = hash.get("userId");
        if (val != null) ci.setUserId(Long.parseLong(val.toString()));

        val = hash.get("name");
        if (val != null && !val.toString().isEmpty()) ci.setName(val.toString());

        val = hash.get("quantity");
        if (val != null && !val.toString().isEmpty()) {
            ci.setQuantity(new BigDecimal(val.toString()));
        }

        val = hash.get("unit");
        if (val != null && !val.toString().isEmpty()) ci.setUnit(val.toString());

        val = hash.get("category");
        if (val != null && !val.toString().isEmpty()) ci.setCategory(val.toString());

        val = hash.get("storageCondition");
        if (val != null && !val.toString().isEmpty()) ci.setStorageCondition(val.toString());

        val = hash.get("expireDate");
        if (val != null && !val.toString().isEmpty()) {
            ci.setExpireDate(LocalDate.parse(val.toString()));
        }

        val = hash.get("createdAt");
        if (val != null && !val.toString().isEmpty()) {
            ci.setCreatedAt(LocalDateTime.parse(val.toString()));
        }

        val = hash.get("readed");
        if (val != null) ci.setReaded("true".equals(val.toString()));

        val = hash.get("status");
        if (val != null && !val.toString().isEmpty()) {
            ci.setStatus(Integer.parseInt(val.toString()));
        }

        return ci;
    }
}
