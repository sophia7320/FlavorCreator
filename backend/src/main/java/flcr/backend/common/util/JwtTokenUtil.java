package flcr.backend.common.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 令牌工具类
 * 负责生成、验证和解析 JWT 令牌
 */
@Slf4j
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret:FlavorCreatorSecretKey2026}")
    private String secret;

    @Value("${jwt.expiration:7200000}")
    private long expiration; // 默认 2 小时

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration; // 默认 7 天

    /**
     * 生成访问令牌
     * @param userId 用户 ID
     * @param openid 用户 openid
     * @return JWT token
     */
    public String generateToken(Long userId, String openid) {
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("openid", openid)
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 生成刷新令牌
     * @param userId 用户 ID
     * @param openid 用户 openid
     * @return JWT refresh token
     */
    public String generateRefreshToken(Long userId, String openid) {
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("openid", openid)
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshExpiration))
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 验证令牌
     * @param token JWT token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            log.error("JWT 验证失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 从令牌中获取用户 ID
     * @param token JWT token
     * @return 用户 ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("userId").asLong();
        } catch (Exception e) {
            log.error("从 token 中获取用户 ID 失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从令牌中获取 OpenID
     * @param token JWT token
     * @return OpenID
     */
    public String getOpenidFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("openid").asString();
        } catch (Exception e) {
            log.error("从 token 中获取 openid 失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查令牌是否过期
     * @param token JWT token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}