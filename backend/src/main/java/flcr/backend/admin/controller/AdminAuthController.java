package flcr.backend.admin.controller;

import flcr.backend.admin.DTO.request.AdminLoginRequestDTO;
import flcr.backend.admin.DTO.request.AdminRefreshTokenRequestDTO;
import flcr.backend.admin.DTO.response.AdminLoginResponseDTO;
import flcr.backend.admin.service.AdminAuthService;
import flcr.backend.common.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public Response<AdminLoginResponseDTO> login(@Valid @RequestBody AdminLoginRequestDTO request) {
        AdminLoginResponseDTO result = adminAuthService.login(request);
        return Response.success("登录成功", result);
    }

    @PostMapping("/refresh")
    public Response<AdminLoginResponseDTO> refresh(@Valid @RequestBody AdminRefreshTokenRequestDTO request) {
        AdminLoginResponseDTO result = adminAuthService.refreshToken(request.getRefreshToken());
        return Response.success("刷新成功", result);
    }

    @PostMapping("/logout")
    public Response<Void> logout(@Valid @RequestBody AdminRefreshTokenRequestDTO request) {
        adminAuthService.logout(request.getRefreshToken());
        return Response.success("退出成功", null);
    }
}
