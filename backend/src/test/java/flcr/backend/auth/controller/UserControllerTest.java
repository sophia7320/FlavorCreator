package flcr.backend.auth.controller;

import flcr.backend.auth.DTO.request.LoginRequestDTO;
import flcr.backend.auth.DTO.request.LogoutRequestDTO;
import flcr.backend.auth.DTO.request.RefreshTokenRequestDTO;
import flcr.backend.auth.DTO.response.LoginResponseDTO;
import flcr.backend.auth.service.UserService;
import flcr.backend.common.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
    @InjectMocks private UserController userController;

    @Test
    @DisplayName("登录成功返回token")
    void testLogin_Success() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setCode("wx_code");

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken("jwt_token");
        when(userService.login(request)).thenReturn(response);

        Response<LoginResponseDTO> result = userController.login(request);
        assertEquals(200, result.getCode());
        assertEquals("jwt_token", result.getData().getToken());
    }

    @Test
    @DisplayName("刷新Token成功")
    void testRefresh_Success() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
        request.setRefreshToken("old_refresh");

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken("new_token");
        when(userService.refreshToken("old_refresh")).thenReturn(response);

        Response<LoginResponseDTO> result = userController.refresh(request);
        assertEquals(200, result.getCode());
        assertEquals("new_token", result.getData().getToken());
    }

    @Test
    @DisplayName("退出登录调用Service")
    void testLogout_Success() {
        LogoutRequestDTO request = new LogoutRequestDTO();
        request.setRefreshToken("rt_123");

        Response<Void> result = userController.logout(request);

        assertEquals(200, result.getCode());
        verify(userService).logout("rt_123");
    }
}
