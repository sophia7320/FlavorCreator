package flcr.backend.auth.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import flcr.backend.auth.DTO.request.LoginDTO;
import flcr.backend.auth.DTO.response.LoginResponseDTO;
import flcr.backend.auth.DTO.response.PhoneBindResponseDTO;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.auth.service.UserService;
import flcr.backend.common.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final WxMaService wxMaService;
    private final JwtTokenUtil jwtTokenUtil;

    public UserServiceImpl(WxMaService wxMaService, JwtTokenUtil jwtTokenUtil) {
        this.wxMaService = wxMaService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public LoginResponseDTO login(LoginDTO request) throws WxErrorException {
        String code = request.getCode();

        // 1. 调用微信 code2Session 接口获取 openid 和 session_key
        WxMaJscode2SessionResult sessionResult = wxMaService.getUserService().getSessionInfo(code);

        if (sessionResult.getOpenid() == null) {
            log.error("微信 code2Session 失败：{}", sessionResult);
            throw new RuntimeException("微信登录失败，无法获取 OpenID");
        }

        String openid = sessionResult.getOpenid();
        String unionid = sessionResult.getUnionid();
        log.info("微信登录成功，openid: {}, unionid: {}", openid, unionid);

        // 2. 查询或创建用户
        User user = getOrCreateUser(openid, unionid, request);

        // 3. 生成 token
        String token = jwtTokenUtil.generateToken((long) user.getId(), user.getOpenid());
        String refreshToken = jwtTokenUtil.generateRefreshToken((long) user.getId(), user.getOpenid());

        // 4. 判断是否需要绑定手机号
        boolean needBindPhone = user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty();

        // 5. 构建响应
        return LoginResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(7200L)
                .needBindPhone(needBindPhone)
                .user(LoginResponseDTO.UserInfoVO.builder()
                        .id((long) user.getId())
                        .openid(user.getOpenid())
                        .unionid(user.getUnionid())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .phone(user.getPhoneNumber())
                        .gender(user.getGender())
                        .isNewUser(user.getCreatedAt() != null &&
                            user.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1)))
                        .build())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PhoneBindResponseDTO bindPhoneNumber(Long userId, String code) throws WxErrorException {
        // 1. 调用微信接口获取手机号
        WxMaPhoneNumberInfo phoneNumberInfo = wxMaService.getUserService().getPhoneNumber( code);

        if (phoneNumberInfo == null) {
            log.error("获取手机号失败");
            throw new RuntimeException("获取手机号失败");
        }

        String phoneNumber = phoneNumberInfo.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            log.error("获取到的手机号为空");
            throw new RuntimeException("获取手机号失败");
        }
        log.info("获取到用户手机号：{}", phoneNumber);

        // 2. 更新用户手机号
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setPhoneNumber(phoneNumber);
        this.updateById(user);

        // 3. 构建响应
        return PhoneBindResponseDTO.builder()
                .phoneNumber(phoneNumber)
                .purePhoneNumber(phoneNumberInfo.getPurePhoneNumber())
                .countryCode(phoneNumberInfo.getCountryCode())
                .build();
    }

    /**
     * 根据 openid 查询用户，不存在则创建
     */
    private User getOrCreateUser(String openid, String unionid, LoginDTO request) {
        // 1. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = this.getOne(wrapper);

        if (user != null) {
            log.info("老用户登录，userId: {}", user.getId());
            return user;
        }

        // 2. 创建新用户
        user = new User();
        user.setOpenid(openid);
        user.setUnionid(unionid);

        // 设置用户信息（如果前端传了）
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

        return user;
    }
}
