package flcr.backend.common.service;

public interface SmsService {

    /**
     * 发送验证码到指定手机号，验证码存入 Redis（5 分钟有效）
     * @param phone 手机号
     */
    void sendCode(String phone);

    /**
     * 校验验证码，校验通过后删除已使用的验证码
     * @param phone 手机号
     * @param code  用户输入的验证码
     * @return true=通过，false=不正确或已过期
     */
    boolean verifyCode(String phone, String code);
}