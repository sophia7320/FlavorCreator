package flcr.backend.admin.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminRefreshTokenRequestDTO {
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
