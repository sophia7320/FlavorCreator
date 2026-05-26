package flcr.backend.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.service.FileStorageService;
import flcr.backend.common.service.ImageModerationService;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.LikeMapper;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.user.DTO.request.UpdateUserInfoRequestDTO;
import flcr.backend.user.DTO.response.UserInfoResponseDTO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserInfoServiceImplTest {

    @Mock private UserMapper userMapper;
    @Mock private RecipeMapper recipeMapper;
    @Mock private LikeMapper likeMapper;
    @Mock private CollectionMapper collectionMapper;
    @Mock private ObjectMapper objectMapper;
    @Mock private FileStorageService fileStorageService;
    @Mock private ImageModerationService imageModerationService;
    @InjectMocks private UserInfoServiceImpl userInfoService;

    private static final Long USER_ID = 1001L;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(USER_ID);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("getInfo成功返回脱敏手机和统计")
    void testGetInfo_Success() {
        User user = buildUser();
        user.setPhoneNumber("13800138000");
        when(userMapper.selectById(USER_ID)).thenReturn(user);
        when(recipeMapper.selectList(any())).thenReturn(Collections.emptyList());

        UserInfoResponseDTO result = userInfoService.getInfo();
        assertEquals("138****8000", result.getPhone());
        assertEquals("测试昵称", result.getNickname());
    }

    @Test
    @DisplayName("getInfo用户不存在抛异常")
    void testGetInfo_NotFound() {
        when(userMapper.selectById(USER_ID)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> userInfoService.getInfo());
        assertEquals(ResultCode.USER_NOT_EXIST, ex.getCode());
    }

    @Test
    @DisplayName("updateInfo更新昵称成功")
    void testUpdateInfo_Nickname() {
        User user = buildUser();
        when(userMapper.selectById(USER_ID)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(recipeMapper.selectList(any())).thenReturn(Collections.emptyList());

        UpdateUserInfoRequestDTO request = new UpdateUserInfoRequestDTO();
        request.setNickname("新昵称");

        UserInfoResponseDTO result = userInfoService.updateInfo(request);
        assertEquals("新昵称", user.getNickname());
    }

    @Test
    @DisplayName("updateInfo更新偏好JSON序列化")
    void testUpdateInfo_Preferences() throws Exception {
        User user = buildUser();
        when(userMapper.selectById(USER_ID)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(recipeMapper.selectList(any())).thenReturn(Collections.emptyList());

        UpdateUserInfoRequestDTO.Preferences prefs = new UpdateUserInfoRequestDTO.Preferences();
        prefs.setTaste(List.of("清淡"));
        prefs.setDietary(List.of("低卡"));
        prefs.setCookTime("简单");
        prefs.setDifficulty("简单");

        UpdateUserInfoRequestDTO request = new UpdateUserInfoRequestDTO();
        request.setPreferences(prefs);

        when(objectMapper.writeValueAsString(prefs)).thenReturn("{\"taste\":[\"清淡\"]}");
        when(objectMapper.readValue(anyString(), eq(UpdateUserInfoRequestDTO.Preferences.class)))
                .thenReturn(prefs);

        assertDoesNotThrow(() -> userInfoService.updateInfo(request));
        verify(objectMapper).writeValueAsString(prefs);
    }

    @Test
    @DisplayName("uploadAvatar成功返回URL")
    void testUploadAvatar_Success() {
        org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        when(fileStorageService.store(file, "avatar")).thenReturn("/uploads/avatar/202604/uuid.jpg");
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser());
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        String url = userInfoService.uploadAvatar(file);
        assertEquals("/uploads/avatar/202604/uuid.jpg", url);
    }

    @Test
    @DisplayName("uploadBackground成功返回URL")
    void testUploadBackground_Success() {
        org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        when(fileStorageService.store(file, "background")).thenReturn("/uploads/background/202604/uuid.jpg");
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser());
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        String url = userInfoService.uploadBackground(file);
        assertEquals("/uploads/background/202604/uuid.jpg", url);
    }

    private User buildUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setOpenid("test_openid");
        user.setNickname("测试昵称");
        user.setAvatar("https://example.com/avatar.jpg");
        user.setGender(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
