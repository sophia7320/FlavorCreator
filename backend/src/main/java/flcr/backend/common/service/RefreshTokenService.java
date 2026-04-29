package flcr.backend.common.service;

public interface RefreshTokenService {

    void store(Long userId, String openid, String refreshToken);

    RefreshTokenData get(String refreshToken);

    void delete(String refreshToken);

    record RefreshTokenData(Long userId, String openid) {}
}
