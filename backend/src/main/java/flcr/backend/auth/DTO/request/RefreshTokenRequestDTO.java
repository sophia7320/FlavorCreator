package flcr.backend.auth.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
