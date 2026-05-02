package flcr.backend.auth.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import flcr.backend.auth.DTO.request.LoginRequestDTO;
import flcr.backend.auth.DTO.response.LoginResponseDTO;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.auth.service.UserService;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.service.RefreshTokenService;
import flcr.backend.common.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final WxMaService wxMaService;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenService refreshTokenService;

    private record UserWithStatus(User user, boolean newUser) {}

    @Transactional
    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        String code = request.getCode();

        // 1. 调用微信 code2Session 接口获取 openid 和 session_key
        WxMaJscode2SessionResult sessionResult;
        try {
            sessionResult = wxMaService.getUserService().getSessionInfo(code);
        } catch (WxErrorException e) {
            log.error("微信 code2Session 失败", e);
            throw new BusinessException(ResultCode.WX_CODE_ERROR, "微信登录失败");
        }

        if (sessionResult.getOpenid() == null) {
            log.error("微信 code2Session 返回空 openid: {}", sessionResult);
            throw new BusinessException(ResultCode.WX_CODE_ERROR, "微信登录失败，无法获取 OpenID");
        }

        String openid = sessionResult.getOpenid();
        String unionid = sessionResult.getUnionid();
        log.info("微信登录成功，openid: {}, unionid: {}", openid, unionid);

        // 2. 查询或创建用户
        UserWithStatus userWithStatus = getOrCreateUser(openid, unionid, request);
        User user = userWithStatus.user();
        boolean isNewUser = userWithStatus.newUser();

        // 3. 生成 token
        String token = jwtTokenUtil.generateToken((long) user.getId(), user.getOpenid());
        String refreshToken = UUID.randomUUID().toString();

        refreshTokenService.store((long) user.getId(), user.getOpenid(), refreshToken);

        // 4. 构建响应
        return LoginResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(300L)
                .isNewUser(isNewUser)
                .user(LoginResponseDTO.UserInfo.builder()
                        .id((long) user.getId())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .phone(user.getPhoneNumber())
                        .gender(user.getGender())
                        .build())
                .build();
    }

    @Override
    public LoginResponseDTO refreshToken(String refreshTokenStr) {
        if (refreshTokenStr == null || refreshTokenStr.isEmpty()) {
            log.error("刷新 token 为空");
            throw new BusinessException(ResultCode.PARAM_ERROR, "刷新 token 不能为空");
        }

        RefreshTokenService.RefreshTokenData data = refreshTokenService.get(refreshTokenStr);
        if (data == null) {
            log.error("刷新 token 无效或已过期");
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "刷新 token 无效或已过期");
        }

        User user = this.getById(data.userId());
        if (user == null || !data.openid().equals(user.getOpenid())) {
            log.error("用户不存在或 openid 不匹配，userId: {}", data.userId());
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        refreshTokenService.delete(refreshTokenStr);

        String newToken = jwtTokenUtil.generateToken(data.userId(), user.getOpenid());
        String newRefreshToken = UUID.randomUUID().toString();
        refreshTokenService.store(data.userId(), user.getOpenid(), newRefreshToken);

        return LoginResponseDTO.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .expiresIn(300L)
                .needBindPhone(false)
                .isNewUser(false)
                .user(LoginResponseDTO.UserInfo.builder()
                        .id(data.userId())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .phone(user.getPhoneNumber())
                        .gender(user.getGender())
                        .build())
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return;
        }
        RefreshTokenService.RefreshTokenData data = refreshTokenService.get(refreshToken);
        if (data != null && data.userId().equals(UserContext.getUserId())) {
            refreshTokenService.delete(refreshToken);
        }
    }

    private UserWithStatus getOrCreateUser(String openid, String unionid, LoginRequestDTO request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = this.getOne(wrapper);

        if (user != null) {
            log.info("老用户登录，userId: {}", user.getId());
            return new UserWithStatus(user, false);
        }

        user = new User();
        user.setOpenid(openid);
        user.setUnionid(unionid);

        if (request.getUserInfo() != null) {
            user.setNickname(request.getUserInfo().getNickName());
            user.setAvatar(request.getUserInfo().getAvatarUrl());
            if (request.getUserInfo().getGender() != null) {
                user.setGender(Integer.parseInt(request.getUserInfo().getGender()));
            }
        } else {
            user.setNickname("创味机用户");
            user.setGender(0);
        }

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        this.save(user);
        log.info("新用户注册，userId: {}", user.getId());

        return new UserWithStatus(user, true);
    }
}
