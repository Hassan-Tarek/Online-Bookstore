package com.bookstore.security.token;

import com.bookstore.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TokenStore {

    private static final String VERIFICATION_PREFIX = "auth:verification:";
    private static final String PASSWORD_RESET_PREFIX = "auth:reset:";
    private static final String REFRESH_PREFIX = "auth:refresh:";
    private static final String BLACKLIST_PREFIX = "auth:blacklist:";

    private final StringRedisTemplate stringRedisTemplate;
    private final AppProperties appProperties;

    public void saveVerificationToken(String email, String token) {
        stringRedisTemplate.opsForValue()
                .set(VERIFICATION_PREFIX + email, token, appProperties.ttl().auth().verification());
        stringRedisTemplate.opsForValue()
                .set(VERIFICATION_PREFIX + token, email, appProperties.ttl().auth().verification());
    }

    public String getVerificationToken(String email) {
        return stringRedisTemplate.opsForValue()
                .get(VERIFICATION_PREFIX + email);
    }

    public String getVerificationEmail(String token) {
        return stringRedisTemplate.opsForValue()
                .get(VERIFICATION_PREFIX + token);
    }

    public void deleteVerificationToken(String token) {
        String email = getVerificationEmail(token);
        stringRedisTemplate.delete(VERIFICATION_PREFIX + email);
        stringRedisTemplate.delete(VERIFICATION_PREFIX + token);
    }

    public void savePasswordResetToken(String email, String token) {
        stringRedisTemplate.opsForValue()
                .set(PASSWORD_RESET_PREFIX + email, token, appProperties.ttl().auth().passwordReset());
        stringRedisTemplate.opsForValue()
                .set(PASSWORD_RESET_PREFIX + token, email, appProperties.ttl().auth().passwordReset());
    }

    public String getPasswordResetToken(String email) {
        return stringRedisTemplate.opsForValue()
                .get(PASSWORD_RESET_PREFIX + email);
    }

    public String getPasswordResetEmail(String token) {
        return stringRedisTemplate.opsForValue()
                .get(PASSWORD_RESET_PREFIX + token);
    }

    public void deletePasswordResetToken(String token) {
        String email = getPasswordResetEmail(token);
        stringRedisTemplate.delete(PASSWORD_RESET_PREFIX + email);
        stringRedisTemplate.delete(PASSWORD_RESET_PREFIX + token);
    }

    public void saveRefreshToken(String jti, String refreshToken) {
        stringRedisTemplate.opsForValue()
                .set(REFRESH_PREFIX + jti, refreshToken, appProperties.ttl().auth().refresh());
    }

    public String getRefreshToken(String jti) {
        return stringRedisTemplate.opsForValue()
                .get(REFRESH_PREFIX + jti);
    }

    public void deleteRefreshToken(String jti) {
        stringRedisTemplate.delete(REFRESH_PREFIX + jti);
    }

    public void blacklistToken(String jti, String token) {
        stringRedisTemplate.opsForValue()
                .set(BLACKLIST_PREFIX + jti, token, appProperties.ttl().auth().access());
    }

    public boolean isBlacklisted(String jti) {
        return stringRedisTemplate.hasKey(BLACKLIST_PREFIX + jti);
    }
}
