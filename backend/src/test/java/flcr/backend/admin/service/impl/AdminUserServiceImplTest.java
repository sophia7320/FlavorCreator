package flcr.backend.admin.service.impl;

import flcr.backend.admin.DTO.request.AdminUserListRequestDTO;
import flcr.backend.admin.DTO.response.AdminUserResponseDTO;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock private UserMapper userMapper;
    @InjectMocks private AdminUserServiceImpl adminUserService;

    @Test
    @DisplayName("getUserDetail成功")
    void testGetUserDetail_Success() {
        when(userMapper.selectById(1L)).thenReturn(buildUser(1L));

        AdminUserResponseDTO result = adminUserService.getUserDetail(1L);
        assertEquals("测试用户", result.getNickname());
    }

    @Test
    @DisplayName("getUserDetail用户不存在抛异常")
    void testGetUserDetail_NotFound() {
        when(userMapper.selectById(99L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminUserService.getUserDetail(99L));
        assertEquals(ResultCode.RESOURCE_NOT_EXIST, ex.getCode());
    }

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setNickname("测试用户");
        user.setAvatar("/avatar.jpg");
        user.setPhoneNumber("13800138000");
        user.setGender(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
