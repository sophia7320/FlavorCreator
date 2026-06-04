package flcr.backend.auth.controller;

import flcr.backend.auth.DTO.request.LoginRequestDTO;
import flcr.backend.auth.DTO.request.LogoutRequestDTO;
import flcr.backend.auth.DTO.request.RefreshTokenRequestDTO;
import flcr.backend.auth.DTO.response.LoginResponseDTO;
import flcr.backend.auth.service.UserService;
import flcr.backend.common.aop.Public;
import flcr.backend.common.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Public
    @PostMapping("/login-wx")
    public Response<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO result = userService.login(request);
        return Response.success("登录成功", result);
    }

    @Public
    @PostMapping("/refresh")
    public Response<LoginResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        LoginResponseDTO result = userService.refreshToken(request.getRefreshToken());
        return Response.success("刷新成功", result);
    }

    @PostMapping("/logout")
    public Response<Void> logout(@Valid @RequestBody LogoutRequestDTO logoutRequest) {
        userService.logout(logoutRequest.getRefreshToken());
        return Response.success("退出成功", null);
    }
}