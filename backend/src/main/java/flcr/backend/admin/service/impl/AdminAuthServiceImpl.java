package flcr.backend.admin.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flcr.backend.admin.DTO.request.AdminLoginRequestDTO;
import flcr.backend.admin.DTO.response.AdminLoginResponseDTO;
import flcr.backend.admin.entity.Admin;
import flcr.backend.admin.mapper.AdminMapper;
import flcr.backend.admin.service.AdminAuthService;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminMapper adminMapper;

    @Value("${admin.jwt.secret:AdminSecretKey2026}")
    private String jwtSecret;

    @Value("${admin.jwt.expiration:7200000}")
    private long jwtExpiration;

    @Value("${admin.jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    @Override
    @Transactional
    public AdminLoginResponseDTO login(AdminLoginRequestDTO request) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, request.getUsername());
        Admin admin = adminMapper.selectOne(wrapper);

        if (admin == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名或密码错误");
        }

        if (!PasswordUtil.match(request.getPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名或密码错误");
        }

        if ("DISABLED".equals(admin.getStatus())) {
            throw new BusinessException(ResultCode.PERMISSION_ERROR, "账号已被禁用");
        }

        String token = generateAdminToken(admin);
        String refreshToken = UUID.randomUUID().toString();

        return AdminLoginResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(jwtExpiration / 1000)
                .admin(AdminLoginResponseDTO.AdminInfo.builder()
                        .id(admin.getId())
                        .username(admin.getUsername())
                        .role(admin.getRole())
                        .build())
                .build();
    }

    @Override
    public AdminLoginResponseDTO refreshToken(String refreshTokenStr) {
        if (refreshTokenStr == null || refreshTokenStr.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "刷新令牌不能为空");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "刷新令牌功能开发中");
    }

    @Override
    public void logout(String refreshToken) {
        log.info("管理员登出: refreshToken={}", refreshToken);
    }

    public String generateAdminToken(Admin admin) {
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("adminId", admin.getId())
                .withClaim("username", admin.getUsername())
                .withClaim("role", admin.getRole())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpiration))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    public Long getAdminIdFromToken(String token) {
        if (!validateAdminToken(token)) return null;
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("adminId").asLong();
        } catch (Exception e) {
            log.error("从 admin token 中获取 ID 失败: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateAdminToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            log.error("Admin JWT 验证失败: {}", e.getMessage());
            return false;
        }
    }

    public Admin getAdminById(Long adminId) {
        return adminMapper.selectById(adminId);
    }

    public Admin getAdminByUsername(String username) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, username);
        return adminMapper.selectOne(wrapper);
    }
}
