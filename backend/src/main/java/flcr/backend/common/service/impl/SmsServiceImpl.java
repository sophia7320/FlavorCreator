package flcr.backend.common.service.impl;

import flcr.backend.common.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final StringRedisTemplate redisTemplate;
    private static final String CODE_PREFIX = "sms:code:";
    private static final long CODE_TTL = 5; // 5 分钟
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void sendCode(String phone) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        String key = CODE_PREFIX + phone;

        redisTemplate.opsForValue().set(key, code, CODE_TTL, TimeUnit.MINUTES);

        // TODO: 对接短信服务商（阿里云/腾讯云）发送验证码
        log.info("验证码已发送到 {} -> {}", phone, code);
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        String key = CODE_PREFIX + phone;
        String stored = redisTemplate.opsForValue().get(key);

        if (stored == null) {
            return false;
        }
        if (!stored.equals(code)) {
            return false;
        }

        redisTemplate.delete(key);
        return true;
    }
}