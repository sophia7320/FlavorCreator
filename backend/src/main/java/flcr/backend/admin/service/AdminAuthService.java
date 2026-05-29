package flcr.backend.admin.service;

import flcr.backend.admin.DTO.request.AdminLoginRequestDTO;
import flcr.backend.admin.DTO.response.AdminLoginResponseDTO;

public interface AdminAuthService {
    AdminLoginResponseDTO login(AdminLoginRequestDTO request);

    AdminLoginResponseDTO refreshToken(String refreshToken);

    void logout(String refreshToken);
}
