package com.wit.be.infra.redis;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh_token:";

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Refresh Token 저장
     *
     * @param userId 사용자 ID
     * @param refreshToken Refresh Token
     * @param expirationMs 만료 시간 (밀리초)
     */
    public void save(Long userId, String refreshToken, long expirationMs) {
        String key = KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, expirationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Refresh Token 조회
     *
     * @param userId 사용자 ID
     * @return Refresh Token (없으면 null)
     */
    public String findByUserId(Long userId) {
        String key = KEY_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Refresh Token 삭제 (로그아웃)
     *
     * @param userId 사용자 ID
     */
    public void deleteByUserId(Long userId) {
        String key = KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    /**
     * Refresh Token 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    public boolean existsByUserId(Long userId) {
        String key = KEY_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 저장된 Refresh Token과 일치하는지 확인
     *
     * @param userId 사용자 ID
     * @param refreshToken 비교할 Refresh Token
     * @return 일치 여부
     */
    public boolean matches(Long userId, String refreshToken) {
        String storedToken = findByUserId(userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }
}
