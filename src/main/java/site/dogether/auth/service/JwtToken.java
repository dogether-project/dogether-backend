package site.dogether.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class JwtToken {

    public static final String PREFIX = "Bearer ";

    private final String value;

    public JwtToken(final String bearerToken) {
        this.value = extract(bearerToken);
    }

    public JwtToken(final Long memberId, final String secret, final Long expireTime) {
        final long now  = new Date().getTime();
        final Date expiredDate = new Date(now + expireTime);

        this.value = Jwts.builder()
                .claim("member_id", memberId)
                .expiration(expiredDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    public void validate(final String secret) {
        Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parse(value);
    }

    public Long getMemberId(final String secret) {
        final Claims claims = parseClaims(secret);
        return claims.get("member_id", Long.class);
    }

    private Claims parseClaims(final String secret) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(value)
                .getPayload();
    }

    private String extract(final String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(PREFIX)) {
            return bearerToken.substring(PREFIX.length());
        }
        return bearerToken;
    }
}
