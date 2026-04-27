package flcr.backend.common.service.impl;

import flcr.backend.common.service.TokenBlacklistService;
import flcr.backend.common.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public void blacklist(String token) {
        long remainingMs = jwtTokenUtil.getRemainingTime(token);
        if (remainingMs <= 0) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "1", remainingMs, TimeUnit.MILLISECONDS);
            log.info("Token 已加入黑名单，剩余 {} ms", remainingMs);
        } catch (Exception e) {
            log.error("Token 加入黑名单失败", e);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
        } catch (Exception e) {
            log.error("查询黑名单失败，放行请求", e);
            return false;
        }
    }
}
