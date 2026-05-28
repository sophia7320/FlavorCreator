package flcr.backend.admin.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponseDTO {
    private String token;
    private String refreshToken;
    private Long expiresIn;
    private AdminInfo admin;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminInfo {
        private Long id;
        private String username;
        private String role;
    }
}
