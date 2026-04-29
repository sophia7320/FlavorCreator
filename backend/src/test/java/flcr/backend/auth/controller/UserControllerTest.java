package flcr.backend.auth.controller;

import flcr.backend.auth.DTO.request.LoginRequestDTO;
import flcr.backend.auth.DTO.request.RefreshTokenRequestDTO;
import flcr.backend.auth.DTO.response.LoginResponseDTO;
import flcr.backend.auth.service.UserService;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.response.Response;
import flcr.backend.common.service.RefreshTokenService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
    @Mock private RefreshTokenService refreshTokenService;
    @InjectMocks private UserController userController;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

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
    @DisplayName("退出登录校验归属删除RT")
    void testLogout_Success() {
        UserContext.setUserId(1001L);
        UserController.LogoutRequestDTO logoutRequest = new UserController.LogoutRequestDTO("rt_123");

        RefreshTokenService.RefreshTokenData data = new RefreshTokenService.RefreshTokenData(1001L, "openid");
        when(refreshTokenService.get("rt_123")).thenReturn(data);

        Response<Void> result = userController.logout(logoutRequest);

        assertEquals(200, result.getCode());
        verify(refreshTokenService).delete("rt_123");
    }

    @Test
    @DisplayName("退出登录RT不属于当前用户不删除")
    void testLogout_WrongUser() {
        UserContext.setUserId(1001L);
        UserController.LogoutRequestDTO logoutRequest = new UserController.LogoutRequestDTO("rt_other");

        RefreshTokenService.RefreshTokenData data = new RefreshTokenService.RefreshTokenData(1002L, "other");
        when(refreshTokenService.get("rt_other")).thenReturn(data);

        Response<Void> result = userController.logout(logoutRequest);

        assertEquals(200, result.getCode());
        verify(refreshTokenService, never()).delete(anyString());
    }

    @Test
    @DisplayName("退出登录RT不存在不抛异常")
    void testLogout_RtNotFound() {
        UserContext.setUserId(1001L);
        UserController.LogoutRequestDTO logoutRequest = new UserController.LogoutRequestDTO("rt_nonexistent");

        when(refreshTokenService.get("rt_nonexistent")).thenReturn(null);

        Response<Void> result = userController.logout(logoutRequest);

        assertEquals(200, result.getCode());
        verify(refreshTokenService, never()).delete(anyString());
    }

    @Test
    @DisplayName("退出登录refreshToken为空不抛异常")
    void testLogout_NullRefreshToken() {
        UserController.LogoutRequestDTO logoutRequest = new UserController.LogoutRequestDTO(null);

        Response<Void> result = userController.logout(logoutRequest);

        assertEquals(200, result.getCode());
        verify(refreshTokenService, never()).delete(anyString());
    }
}
