package flcr.backend.common.service.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceImplTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;
    @InjectMocks private SmsServiceImpl smsService;

    @Test
    @DisplayName("发送验证码存入Redis")
    void testSendCode() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        smsService.sendCode("13800138000");
        verify(valueOps).set(contains("sms:code:13800138000"), anyString(), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("验证码匹配返回true")
    void testVerifyCode_Matched() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("sms:code:13800138000")).thenReturn("123456");

        assertTrue(smsService.verifyCode("13800138000", "123456"));
        verify(redisTemplate).delete("sms:code:13800138000");
    }

    @Test
    @DisplayName("验证码不存在返回false")
    void testVerifyCode_NotFound() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("sms:code:13800138000")).thenReturn(null);

        assertFalse(smsService.verifyCode("13800138000", "000000"));
    }

    @Test
    @DisplayName("验证码不匹配返回false")
    void testVerifyCode_NotMatched() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("sms:code:13800138000")).thenReturn("654321");

        assertFalse(smsService.verifyCode("13800138000", "123456"));
        verify(redisTemplate, never()).delete(anyString());
    }
}
