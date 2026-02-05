package com.wit.be.infra.oauth.validator;

import com.wit.be.common.exception.BusinessException;
import com.wit.be.infra.oauth.dto.ApplePublicKeyResponse;
import com.wit.be.infra.oauth.dto.OAuthUserInfo;
import com.wit.be.infra.oauth.exception.OAuthErrorCode;
import com.wit.be.infra.properties.AppleOAuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleOAuthValidator implements OAuthValidator {

    private final AppleOAuthProperties appleProperties;
    private final RestTemplate restTemplate;

    @Override
    public OAuthUserInfo validate(String idToken) {
        try {
            // 1. Apple 공개키 가져오기
            ApplePublicKeyResponse keys =
                    restTemplate.getForObject(
                            appleProperties.keysUrl(), ApplePublicKeyResponse.class);

            if (keys == null || keys.keys().isEmpty()) {
                throw new BusinessException(OAuthErrorCode.INVALID_APPLE_TOKEN);
            }

            // 2. JWT 헤더에서 kid 추출
            String kid = getKidFromToken(idToken);

            // 3. 매칭되는 공개키 찾기
            ApplePublicKeyResponse.AppleKey appleKey = keys.getMatchingKey(kid);
            if (appleKey == null) {
                throw new BusinessException(OAuthErrorCode.INVALID_APPLE_TOKEN);
            }

            // 4. 공개키 생성
            PublicKey publicKey = generatePublicKey(appleKey);

            // 5. JWT 검증 및 클레임 추출
            Claims claims =
                    Jwts.parser()
                            .verifyWith(publicKey)
                            .build()
                            .parseSignedClaims(idToken)
                            .getPayload();

            // 6. issuer, audience 검증
            validateClaims(claims);

            return OAuthUserInfo.builder()
                    .providerId(claims.getSubject())
                    .email(claims.get("email", String.class))
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple token validation failed", e);
            throw new BusinessException(OAuthErrorCode.INVALID_APPLE_TOKEN);
        }
    }

    private String getKidFromToken(String idToken) {
        String[] parts = idToken.split("\\.");
        if (parts.length != 3) {
            throw new BusinessException(OAuthErrorCode.INVALID_APPLE_TOKEN);
        }

        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
        // Simple parsing - kid 추출
        int kidIndex = headerJson.indexOf("\"kid\"");
        if (kidIndex == -1) {
            throw new BusinessException(OAuthErrorCode.INVALID_APPLE_TOKEN);
        }

        int colonIndex = headerJson.indexOf(":", kidIndex);
        int startQuote = headerJson.indexOf("\"", colonIndex);
        int endQuote = headerJson.indexOf("\"", startQuote + 1);

        return headerJson.substring(startQuote + 1, endQuote);
    }

    private PublicKey generatePublicKey(ApplePublicKeyResponse.AppleKey key) throws Exception {
        byte[] nBytes = Base64.getUrlDecoder().decode(key.n());
        byte[] eBytes = Base64.getUrlDecoder().decode(key.e());

        BigInteger n = new BigInteger(1, nBytes);
        BigInteger e = new BigInteger(1, eBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(n, e);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(spec);
    }

    private void validateClaims(Claims claims) {
        String issuer = claims.getIssuer();
        if (!appleProperties.issuer().equals(issuer)) {
            throw new BusinessException(OAuthErrorCode.INVALID_APPLE_TOKEN);
        }

        // audience 검증 (설정된 경우에만)
        if (appleProperties.audience() != null && !appleProperties.audience().isBlank()) {
            String audience = claims.getAudience().stream().findFirst().orElse(null);
            if (!appleProperties.audience().equals(audience)) {
                throw new BusinessException(OAuthErrorCode.INVALID_APPLE_TOKEN);
            }
        }
    }
}
