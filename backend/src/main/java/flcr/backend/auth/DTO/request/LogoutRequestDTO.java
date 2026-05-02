package flcr.backend.auth.DTO.request;

import lombok.Data;

@Data
public class LogoutRequestDTO {
    private String refreshToken;
}
