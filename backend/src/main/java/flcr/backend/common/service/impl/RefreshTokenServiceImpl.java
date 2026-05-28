package flcr.backend.common.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.common.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "flcr.token.store", havingValue = "redis")
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final String RT_PREFIX = "rt:";
    private static final long REFRESH_TOKEN_TTL_DAYS = 30;
    private static final long REFRESH_TOKEN_TTL_SECONDS = REFRESH_TOKEN_TTL_DAYS * 24 * 60 * 60;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void store(Long userId, String openid, String refreshToken) {
        try {
            RefreshTokenData data = new RefreshTokenData(userId, openid);
            String json = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(RT_PREFIX + refreshToken, json, REFRESH_TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
            log.info("RefreshToken 已存储，userId={}", userId);
        } catch (JsonProcessingException e) {
            log.error("RefreshToken 数据序列化失败", e);
        }
    }

    @Override
    public RefreshTokenData get(String refreshToken) {
        try {
            String json = redisTemplate.opsForValue().get(RT_PREFIX + refreshToken);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, RefreshTokenData.class);
        } catch (JsonProcessingException e) {
            log.error("RefreshToken 数据反序列化失败", e);
            return null;
        }
    }

    @Override
    public void delete(String refreshToken) {
        redisTemplate.delete(RT_PREFIX + refreshToken);
        log.info("RefreshToken 已删除");
    }
}
