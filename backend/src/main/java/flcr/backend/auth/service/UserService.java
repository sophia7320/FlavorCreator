package flcr.backend.auth.service;

import flcr.backend.auth.DTO.request.LoginDTO;
import flcr.backend.auth.DTO.response.LoginResponseDTO;
import flcr.backend.auth.DTO.response.PhoneBindResponseDTO;
import me.chanjar.weixin.common.error.WxErrorException;

public interface UserService {
    LoginResponseDTO login(LoginDTO request) throws WxErrorException;

    PhoneBindResponseDTO bindPhoneNumber(Long userId, String code) throws WxErrorException;

    LoginResponseDTO refreshToken(String refreshToken);
}
