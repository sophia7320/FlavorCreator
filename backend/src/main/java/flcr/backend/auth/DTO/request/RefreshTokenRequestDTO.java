package flcr.backend.auth.DTO.request;

import lombok.Data;

@Data
public class RefreshTokenRequestDTO {
    private String refreshToken;
}
