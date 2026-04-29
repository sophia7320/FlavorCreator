package flcr.backend.auth.controller;

import flcr.backend.auth.DTO.request.LoginRequestDTO;
import flcr.backend.auth.DTO.request.RefreshTokenRequestDTO;
import flcr.backend.auth.DTO.response.LoginResponseDTO;
import flcr.backend.auth.service.UserService;
import flcr.backend.common.aop.RequireAuth;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.response.Response;
import flcr.backend.common.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login-wx")
    public Response<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO result = userService.login(request);
        return Response.success("登录成功", result);
    }

    @PostMapping("/refresh")
    public Response<LoginResponseDTO> refresh(@RequestBody RefreshTokenRequestDTO request) {
        LoginResponseDTO result = userService.refreshToken(request.getRefreshToken());
        return Response.success("刷新成功", result);
    }

    @RequireAuth
    @PostMapping("/logout")
    public Response<Void> logout(@RequestBody LogoutRequestDTO logoutRequest) {
        String refreshToken = logoutRequest.refreshToken();
        if (refreshToken != null && !refreshToken.isEmpty()) {
            RefreshTokenService.RefreshTokenData data = refreshTokenService.get(refreshToken);
            if (data != null && data.userId().equals(UserContext.getUserId())) {
                refreshTokenService.delete(refreshToken);
            }
        }
        return Response.success("退出成功", null);
    }

    public record LogoutRequestDTO(String refreshToken) {}
}
