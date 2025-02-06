package site.dogether.auth.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
        final AuthenticationToken token = new AuthenticationToken(bearerToken);
        try {
            token.validate(secret);
            log.info("토큰 검증에 성공하였습니다.");
        } catch (Exception e) {
            log.info("토큰 검증에 실패하였습니다.");
        }
    }

    public String createToken(Long memberId) {
        final AuthenticationToken token = new AuthenticationToken(memberId, secret, expireTime);
        log.info("토큰을 생성합니다. {}", token.getValue());
        return token.getValue();
    }

    public Long getMemberId(final String token) {
        final Claims claims = parseClaims(token);
        return claims.get("member_id", Long.class);
    }

    private Claims parseClaims(String value) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(value)
                .getPayload();
    }

}
