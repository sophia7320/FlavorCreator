package flcr.backend.auth.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flcr.backend.auth.DTO.request.LoginRequestDTO;
import flcr.backend.auth.DTO.response.LoginResponseDTO;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.service.TokenBlacklistService;
import flcr.backend.common.util.JwtTokenUtil;
import me.chanjar.weixin.common.error.WxErrorException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private WxMaService wxMaService;
    @Mock private WxMaUserService wxMaUserService;
    @Mock private JwtTokenUtil jwtTokenUtil;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private UserMapper userMapper;
    @InjectMocks private UserServiceImpl userService;

    private static final Long USER_ID = 1001L;
    private static final String TEST_OPENID = "test_openid";
    private static final String TEST_TOKEN = "test_jwt_token";
    private static final String TEST_REFRESH_TOKEN = "test_refresh_token";

    @BeforeEach
    void setUp() {
        lenient().when(wxMaService.getUserService()).thenReturn(wxMaUserService);
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @Test
    @DisplayName("老用户登录返回token")
    void testLogin_ExistingUser() throws Exception {
        WxMaJscode2SessionResult sessionResult = new WxMaJscode2SessionResult();
        sessionResult.setOpenid(TEST_OPENID);

        when(wxMaUserService.getSessionInfo(anyString())).thenReturn(sessionResult);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenAnswer(inv -> {
            User u = new User();
            u.setId(USER_ID);
            u.setOpenid(TEST_OPENID);
            u.setNickname("老用户");
            u.setGender(1);
            return u;
        });
        when(jwtTokenUtil.generateToken(eq(USER_ID), eq(TEST_OPENID))).thenReturn(TEST_TOKEN);
        when(jwtTokenUtil.generateRefreshToken(eq(USER_ID), eq(TEST_OPENID))).thenReturn(TEST_REFRESH_TOKEN);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setCode("wx_code_123");

        LoginResponseDTO result = userService.login(request);
        assertEquals(TEST_TOKEN, result.getToken());
        assertEquals(TEST_REFRESH_TOKEN, result.getRefreshToken());
        assertFalse(result.getIsNewUser());
    }

    @Test
    @DisplayName("新用户登录自动注册返回isNewUser")
    void testLogin_NewUser() throws Exception {
        WxMaJscode2SessionResult sessionResult = new WxMaJscode2SessionResult();
        sessionResult.setOpenid("new_openid");

        when(wxMaUserService.getSessionInfo(anyString())).thenReturn(sessionResult);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(999L);
            return 1;
        });
        when(jwtTokenUtil.generateToken(anyLong(), anyString())).thenReturn(TEST_TOKEN);
        when(jwtTokenUtil.generateRefreshToken(anyLong(), anyString())).thenReturn(TEST_REFRESH_TOKEN);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setCode("wx_code_new");

        LoginResponseDTO result = userService.login(request);
        assertTrue(result.getIsNewUser());
    }

    @Test
    @DisplayName("登录微信接口异常抛BusinessException")
    void testLogin_WxError() throws Exception {
        when(wxMaUserService.getSessionInfo(anyString())).thenThrow(new WxErrorException("微信异常"));

        LoginRequestDTO request = new LoginRequestDTO();
        request.setCode("bad_code");

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.login(request));
        assertEquals(ResultCode.WX_CODE_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("登录openid为空抛异常")
    void testLogin_EmptyOpenid() throws Exception {
        WxMaJscode2SessionResult sessionResult = new WxMaJscode2SessionResult();
        sessionResult.setOpenid(null);

        when(wxMaUserService.getSessionInfo(anyString())).thenReturn(sessionResult);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setCode("bad_code");

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.login(request));
        assertEquals(ResultCode.WX_CODE_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("refreshToken有效返回新token")
    void testRefreshToken_Success() {
        when(jwtTokenUtil.validateToken(TEST_REFRESH_TOKEN)).thenReturn(true);
        when(jwtTokenUtil.getUserIdFromToken(TEST_REFRESH_TOKEN)).thenReturn(USER_ID);
        when(jwtTokenUtil.getOpenidFromToken(TEST_REFRESH_TOKEN)).thenReturn(TEST_OPENID);

        User user = new User();
        user.setId(USER_ID);
        user.setOpenid(TEST_OPENID);
        user.setNickname("测试用户");

        when(userMapper.selectById(USER_ID)).thenReturn(user);
        when(jwtTokenUtil.generateToken(USER_ID, TEST_OPENID)).thenReturn("new_token");
        when(jwtTokenUtil.generateRefreshToken(USER_ID, TEST_OPENID)).thenReturn("new_refresh");
        when(jwtTokenUtil.getJtiFromToken(TEST_REFRESH_TOKEN)).thenReturn("old_jti");

        LoginResponseDTO result = userService.refreshToken(TEST_REFRESH_TOKEN);
        assertEquals("new_token", result.getToken());
        assertEquals("new_refresh", result.getRefreshToken());
    }

    @Test
    @DisplayName("refreshToken为空抛异常")
    void testRefreshToken_Null() {
        BusinessException ex = assertThrows(BusinessException.class, () -> userService.refreshToken(null));
        assertEquals(ResultCode.PARAM_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("refreshToken无效抛异常")
    void testRefreshToken_Invalid() {
        when(jwtTokenUtil.validateToken("bad_token")).thenReturn(false);
        BusinessException ex = assertThrows(BusinessException.class, () -> userService.refreshToken("bad_token"));
        assertEquals(ResultCode.USER_NOT_EXIST, ex.getCode());
    }

    @Test
    @DisplayName("refreshToken用户不匹配抛异常")
    void testRefreshToken_UserMismatch() {
        when(jwtTokenUtil.validateToken(TEST_REFRESH_TOKEN)).thenReturn(true);
        when(jwtTokenUtil.getUserIdFromToken(TEST_REFRESH_TOKEN)).thenReturn(USER_ID);
        when(jwtTokenUtil.getOpenidFromToken(TEST_REFRESH_TOKEN)).thenReturn(TEST_OPENID);

        User user = new User();
        user.setId(USER_ID);
        user.setOpenid("different_openid");
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.refreshToken(TEST_REFRESH_TOKEN));
        assertEquals(ResultCode.USER_NOT_EXIST, ex.getCode());
    }
}
