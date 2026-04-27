package flcr.backend.auth.service;

import flcr.backend.auth.DTO.request.LoginDTO;
import flcr.backend.auth.DTO.response.LoginResponseDTO;
import me.chanjar.weixin.common.error.WxErrorException;

public interface UserService {
    LoginResponseDTO login(LoginDTO request) throws WxErrorException;

    LoginResponseDTO refreshToken(String refreshToken);
}
