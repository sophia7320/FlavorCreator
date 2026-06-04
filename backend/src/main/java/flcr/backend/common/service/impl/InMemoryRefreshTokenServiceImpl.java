package flcr.backend.common.service.impl;

import flcr.backend.common.service.RefreshTokenService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnProperty(name = "flcr.token.store", havingValue = "memory", matchIfMissing = true)
public class InMemoryRefreshTokenServiceImpl implements RefreshTokenService {

    private static final long REFRESH_TOKEN_TTL_MS = 30L * 24 * 60 * 60 * 1000; // 30 天

    private final ConcurrentHashMap<String, CacheEntry> tokenStore = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor;

    public InMemoryRefreshTokenServiceImpl() {
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "token-cleanup");
            t.setDaemon(true);
            return t;
        });
        // 每小时清理一次过期 token
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpired, 1, 1, TimeUnit.HOURS);
    }

    private record CacheEntry(RefreshTokenData data, long expireTime) {
    }

    @Override
    public void store(Long userId, String openid, String refreshToken) {
        RefreshTokenData data = new RefreshTokenData(userId, openid);
        CacheEntry entry = new CacheEntry(data, System.currentTimeMillis() + REFRESH_TOKEN_TTL_MS);
        tokenStore.put(refreshToken, entry);
        log.debug("RefreshToken 已存储（内存），userId={}", userId);
    }

    @Override
    public RefreshTokenData get(String refreshToken) {
        CacheEntry entry = tokenStore.get(refreshToken);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() > entry.expireTime) {
            tokenStore.remove(refreshToken, entry);
            return null;
        }
        return entry.data;
    }

    @Override
    public void delete(String refreshToken) {
        tokenStore.remove(refreshToken);
        log.debug("RefreshToken 已删除（内存）");
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        tokenStore.entrySet().removeIf(e -> {
            boolean expired = e.getValue().expireTime < now;
            if (expired) {
                log.debug("清理过期 RefreshToken: {}", e.getKey());
            }
            return expired;
        });
    }

    @PreDestroy
    public void shutdown() {
        cleanupExecutor.shutdown();
        log.info("Token 清理线程已关闭");
    }
}
