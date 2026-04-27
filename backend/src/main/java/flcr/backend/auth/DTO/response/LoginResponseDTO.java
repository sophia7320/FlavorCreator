package flcr.backend.auth.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    /**
     * 访问令牌
     */
    private String token;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 是否需要绑定手机号
     */
    private Boolean needBindPhone;

    /**
     * 是否为新注册用户
     */
    private Boolean isNewUser;

    /**
     * 用户信息
     */
    private UserInfoVO user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoVO {
        private Long id;
        private String nickname;
        private String avatar;
        private String phone;
        private Integer gender;
    }
}