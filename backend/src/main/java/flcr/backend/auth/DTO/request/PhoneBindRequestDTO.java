package flcr.backend.auth.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhoneBindRequestDTO {
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String smsCode;
}
