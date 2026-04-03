package flcr.backend.auth.service;

import flcr.backend.auth.DTO.request.LoginDTO;
import flcr.backend.auth.DTO.response.LoginResponseDTO;
import flcr.backend.auth.DTO.response.PhoneBindResponseDTO;
import me.chanjar.weixin.common.error.WxErrorException;

public interface UserService {
    /**
     * 微信登录
     * @param request 登录请求
     * @return 登录响应
     * @throws WxErrorException 微信 API 异常
     */
    LoginResponseDTO login(LoginDTO request) throws WxErrorException;

    /**
     * 绑定手机号
     * @param userId 用户 ID
     * @param code 微信手机号 code
     * @return 手机号响应
     * @throws WxErrorException 微信 API 异常
     */
    PhoneBindResponseDTO bindPhoneNumber(Long userId, String code) throws WxErrorException;
}
