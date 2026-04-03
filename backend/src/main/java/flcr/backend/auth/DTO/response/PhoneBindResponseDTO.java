package flcr.backend.auth.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 手机号绑定响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneBindResponseDTO {
    /**
     * 手机号
     */
    private String phoneNumber;

    /**
     * 纯手机号（无格式）
     */
    private String purePhoneNumber;

    /**
     * 国家代码
     */
    private String countryCode;
}
