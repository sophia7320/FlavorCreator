package flcr.backend.auth.service;

import flcr.backend.auth.DTO.request.LoginRequestDTO;
import flcr.backend.auth.DTO.response.LoginResponseDTO;

public interface UserService {
    LoginResponseDTO login(LoginRequestDTO request);

    LoginResponseDTO refreshToken(String refreshToken);

    void logout(String refreshToken);
}
