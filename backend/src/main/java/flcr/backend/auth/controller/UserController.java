package flcr.backend.auth.controller;

import flcr.backend.auth.DTO.request.LoginDTO;
import flcr.backend.auth.DTO.request.PhoneBindDTO;
import flcr.backend.auth.DTO.request.RefreshTokenDTO;
import flcr.backend.auth.DTO.response.LoginResponseDTO;
import flcr.backend.auth.DTO.response.PhoneBindResponseDTO;
import flcr.backend.auth.service.UserService;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.response.Response;
import flcr.backend.common.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    public UserController(UserService userService, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * 微信一键登录
     * POST /api/auth/login-wx
     */
    @PostMapping("/login-wx")
    public Response<LoginResponseDTO> login(@RequestBody LoginDTO request) {
        try {
            LoginResponseDTO result = userService.login(request);
            return Response.success("登录成功", result);
        } catch (Exception e) {
            log.error("登录失败", e);
            return Response.error(ResultCode.WX_API_ERROR, "登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取并绑定微信手机号
     * POST /api/auth/phone-wx
     */
    @PostMapping("/phone-wx")
    public Response<PhoneBindResponseDTO> bindPhone(@RequestHeader("Authorization") String authorization,
                                                    @RequestBody PhoneBindDTO request) {
        try {
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtTokenUtil.getUserIdFromToken(token);

            if (userId == null) {
                return Response.error(ResultCode.PARAM_ERROR, "无效的 token");
            }

            PhoneBindResponseDTO response = userService.bindPhoneNumber(userId, request.getCode());
            return Response.success("绑定成功", response);
        } catch (Exception e) {
            log.error("绑定手机号失败", e);
            return Response.error(ResultCode.PHONE_ERROR, "绑定失败：" + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public Response<LoginResponseDTO> refresh(@RequestBody RefreshTokenDTO request) {
        try {
            LoginResponseDTO result = userService.refreshToken(request.getRefreshToken());
            return Response.success("刷新成功", result);
        } catch (Exception e) {
            log.error("刷新 token 失败", e);
            return Response.error(ResultCode.USER_NOT_EXIST, "刷新失败：" + e.getMessage());
        }
    }

    /**
     * 退出登录
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public Response<Void> logout() {
        return Response.success("退出成功", null);
    }
}
