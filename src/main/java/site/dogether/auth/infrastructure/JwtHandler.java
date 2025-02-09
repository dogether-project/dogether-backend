package site.dogether.auth.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtHandler {

    public static final String PREFIX = "Bearer ";

    @Value("${secret.jwt.secret-key}")
    private String secret;
    @Value("${secret.jwt.expire-time}")
    private Long expireTime;

    public void validateToken(final String bearerToken) {
        final String token = extract(bearerToken);
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parse(token);
            log.info("토큰 검증에 성공하였습니다.");
        } catch (Exception e) {
            log.info("토큰 검증에 실패하였습니다.");
        }
    }

    public String parseClaimsOfIdToken(final String idToken, final PublicKey publicKey) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            log.info("IdToken 검증에 실패하였습니다.");
        }
        return idToken;
    }

    public String createToken(Long memberId) {
        final long now  = new Date().getTime();
        final Date expiredDate = new Date(now + expireTime);

        final String token = Jwts.builder()
                .claim("member_id", memberId)
                .expiration(expiredDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();

        log.info("토큰을 생성합니다. {}", token);
        return token;
    }

    public String createClientSecret(final String keyId, final String teamId, final Date expireDate,
                                     final String audience, final String clientId, final PrivateKey privateKey) {
        return Jwts.builder()
                .header()
                    .add("alg", Jwts.SIG.ES256).add("kid", keyId).and()
                .issuer(teamId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expireDate)
                .audience().add(audience).and()
                .subject(clientId)
                .signWith(privateKey, Jwts.SIG.ES256)
                .compact();
    }

    public Long getMemberId(final String bearerToken) {
        String token = extract(bearerToken);
        final Claims claims = parseClaims(token);
        return claims.get("member_id", Long.class);
    }

    private String extract(final String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(PREFIX)) {
            return bearerToken.substring(PREFIX.length());
        }
        return bearerToken;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
