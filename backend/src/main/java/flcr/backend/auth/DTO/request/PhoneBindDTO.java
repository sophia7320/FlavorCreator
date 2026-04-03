package flcr.backend.auth.DTO.request;

import lombok.Data;

/**
 * 手机号绑定请求 DTO
 */
@Data
public class PhoneBindDTO {
    /**
     * 微信返回的 code
     */
    private String code;
}