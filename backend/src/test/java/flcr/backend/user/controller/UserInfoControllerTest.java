package flcr.backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.user.DTO.request.UpdateUserInfoRequestDTO;
import flcr.backend.user.DTO.response.UserInfoResponseDTO;
import flcr.backend.user.service.UserInfoService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户信息控制器测试")
class UserInfoControllerTest {

    @Mock private UserInfoService userInfoService;
    @InjectMocks private UserInfoController controller;

    @Test
    @DisplayName("getInfo返回用户信息")
    void testGetInfo_ReturnsUserInfo() {
        UserInfoResponseDTO rsp = UserInfoResponseDTO.builder().nickname("测试").build();
        when(userInfoService.getInfo()).thenReturn(rsp);

        assertEquals("测试", controller.getInfo().getData().getNickname());
    }

    @Test
    @DisplayName("updateInfo返回更新后信息")
    void testUpdateInfo_ReturnsUpdatedInfo() {
        UpdateUserInfoRequestDTO req = new UpdateUserInfoRequestDTO();
        req.setNickname("新昵称");
        UserInfoResponseDTO rsp = UserInfoResponseDTO.builder().nickname("新昵称").build();
        when(userInfoService.updateInfo(req)).thenReturn(rsp);

        assertEquals("新昵称", controller.updateInfo(req).getData().getNickname());
    }

    @Test
    @DisplayName("uploadAvatar返回URL")
    void testUploadAvatar_ReturnsUrl() {
        MultipartFile file = mock(MultipartFile.class);
        when(userInfoService.uploadAvatar(file)).thenReturn("/uploads/test.jpg");

        assertEquals("/uploads/test.jpg", controller.uploadAvatar(file).getData().get("avatarUrl"));
    }

    @Test
    @DisplayName("uploadBackground返回URL")
    void testUploadBackground_ReturnsUrl() {
        MultipartFile file = mock(MultipartFile.class);
        when(userInfoService.uploadBackground(file)).thenReturn("/uploads/bg.jpg");

        assertEquals("/uploads/bg.jpg", controller.uploadBackground(file).getData().get("backgroundUrl"));
    }
}
