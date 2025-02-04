package site.dogether.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtHandler {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expire-time}")
    private Long expireTime;

    public void validateToken(final String bearerToken) {
        final String token = bearerToken.substring("Bearer ".length()).trim();
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parse(token);
            log.info("토큰이 유효합니다.");
        } catch (Exception e) {
            log.info("토큰이 유효하지 않습니다.");
        }
    }

    public String createToken(Long id) {
        final long now  = new Date().getTime();
        final Date expiredDate = new Date(now + expireTime);

        final String accessToken = Jwts.builder()
                .claim("member_id", id)
                .expiration(expiredDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();

        log.info("토큰이 발급되었습니다.");

        return accessToken;
    }

    public Long getMemberId(final String token) {
        final Claims claims = parseClaims(token);
        return claims.get("member_id", Long.class);
    }

    private Claims parseClaims(final String token) {
        log.info("토큰을 파싱합니다. {}", token);
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token.trim())
                .getPayload();
    }
}
