package flcr.backend.auth.controller;

import flcr.backend.auth.DTO.request.LoginDTO;
import flcr.backend.auth.DTO.request.RefreshTokenDTO;
import flcr.backend.auth.DTO.response.LoginResponseDTO;
import flcr.backend.auth.service.UserService;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 微信一键登录
     * POST /api/auth/login-wx
     */
    @PostMapping("/login-wx")
    public Response<LoginResponseDTO> login(@RequestBody LoginDTO request) {
        try {
            LoginResponseDTO result = userService.login(request);
            return Response.success("登录成功", result);
        } catch (WxErrorException e) {
            log.error("微信接口调用失败", e);
            return Response.error(ResultCode.WX_API_ERROR, "微信接口调用失败");
        } catch (BusinessException e) {
            log.error("登录失败: {}", e.getMessage());
            return Response.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("登录失败", e);
            return Response.error(ResultCode.SYSTEM_ERROR, "系统错误");
        }
    }

    /**
     * 刷新 Token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public Response<LoginResponseDTO> refresh(@RequestBody RefreshTokenDTO request) {
        try {
            LoginResponseDTO result = userService.refreshToken(request.getRefreshToken());
            return Response.success("刷新成功", result);
        } catch (BusinessException e) {
            log.error("刷新失败: {}", e.getMessage());
            return Response.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("刷新 token 失败", e);
            return Response.error(ResultCode.SYSTEM_ERROR, "系统错误");
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
