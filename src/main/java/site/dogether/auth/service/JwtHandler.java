package site.dogether.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtHandler {

    private static final String TEMP_JWT_SECRET_KEY = "secretsecretsecretsecretsecretsecretsecretsec";
    private static final Long EXPIRE_TIME = 365 * 24 * 60 * 60 * 1000L;

    public String validateToken(String bearerToken) {
        String token = bearerToken.substring("Bearer ".length()).trim();
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(TEMP_JWT_SECRET_KEY.getBytes()))
                    .build()
                    .parse(token);
            log.info("토큰이 유효합니다.");
            return token;
        } catch (Exception e) {
            log.info("토큰이 유효하지 않습니다.");
        }
        return null;
    }

    public String createToken(Long id) {
        long now  = new Date().getTime();
        Date expiredDate = new Date(now + EXPIRE_TIME);

        String accessToken = Jwts.builder()
                .claim("member_id", id)
                .expiration(expiredDate)
                .signWith(Keys.hmacShaKeyFor(TEMP_JWT_SECRET_KEY.getBytes()))
                .compact();

        log.info("토큰이 발급되었습니다.");

        return accessToken;
    }

    public Long getMemberId(String token) {
        Claims claims = parseClaims(token);
        return claims.get("member_id", Long.class);
    }

    private Claims parseClaims(String token) {
        log.info("토큰을 파싱합니다. {}", token);
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(TEMP_JWT_SECRET_KEY.getBytes()))
                .build()
                .parseSignedClaims(token.trim())
                .getPayload();
    }
}
