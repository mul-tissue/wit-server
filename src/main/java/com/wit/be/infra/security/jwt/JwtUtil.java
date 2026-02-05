package com.wit.be.infra.security.jwt;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.wit.be.common.exception.BusinessException;
import com.wit.be.infra.properties.JwtProperties;
import com.wit.be.infra.security.exception.JwtErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private static final String AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String TYPE = "type";
    private static final String AUTHORITIES = "authorities";
    private static final long MS_TO_SEC = 1000L;

    private final JwtProperties jwtProperties;

    private SecretKey secretKey;

    @PostConstruct
    protected void initSecretKey() {
        this.secretKey = hmacShaKeyFor(jwtProperties.secret().getBytes(UTF_8));
    }

    /** 액세스 토큰 생성 */
    public String generateAccessToken(Long userId, List<String> roles) {
        return generateToken(userId, roles, jwtProperties.accessTokenExpiration(), "access");
    }

    /** 리프레시 토큰 생성 */
    public String generateRefreshToken(Long userId, List<String> roles) {
        return generateToken(userId, roles, jwtProperties.refreshTokenExpiration(), "refresh");
    }

    /** 토큰 유효성 검증 */
    public void validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(JwtErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(JwtErrorCode.INVALID_TOKEN);
        }
    }

    /** 액세스 토큰에서 Authentication 객체 생성 */
    public Authentication getAuthentication(String accessToken) {
        Claims claims = getClaims(accessToken);

        String subject = claims.getSubject();
        List<GrantedAuthority> authorities = getAuthorities(claims);

        User principal = new User(subject, "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /** 토큰에서 사용자 ID 추출 */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /** 만료된 토큰에서도 사용자 ID 추출 (로그아웃용) */
    public Long getUserIdFromTokenAllowExpired(String token) {
        Claims claims = getClaimsAllowExpired(token);
        return Long.parseLong(claims.getSubject());
    }

    /** HTTP 요청 헤더에서 액세스 토큰 추출 */
    public String getAccessTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION);

        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }

        return null;
    }

    /** 토큰 남은 유효 시간 계산 (초 단위) */
    public long getTokenExpirationTime(String token) {
        Claims claims = getClaimsAllowExpired(token);
        Date expiration = claims.getExpiration();
        Date now = new Date();

        return (expiration.getTime() - now.getTime()) / MS_TO_SEC;
    }

    /** Access Token 만료 시간 반환 (밀리초) */
    public long getAccessTokenExpiration() {
        return jwtProperties.accessTokenExpiration();
    }

    /** Refresh Token 만료 시간 반환 (밀리초) */
    public long getRefreshTokenExpiration() {
        return jwtProperties.refreshTokenExpiration();
    }

    private String generateToken(Long userId, List<String> roles, Long expiration, String type) {
        String authorities = String.join(",", roles);

        Date issuedTime = new Date(System.currentTimeMillis());
        Date expiredTime = new Date(issuedTime.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(TYPE, type)
                .claim(AUTHORITIES, authorities)
                .issuer(jwtProperties.issuer())
                .issuedAt(issuedTime)
                .expiration(expiredTime)
                .signWith(secretKey)
                .compact();
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(JwtErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(JwtErrorCode.INVALID_TOKEN);
        }
    }

    private Claims getClaimsAllowExpired(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(JwtErrorCode.INVALID_TOKEN);
        }
    }

    private List<GrantedAuthority> getAuthorities(Claims claims) {
        String authoritiesClaim = claims.get(AUTHORITIES, String.class);

        if (authoritiesClaim == null || authoritiesClaim.isBlank()) {
            return List.of();
        }

        return Stream.of(authoritiesClaim.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
