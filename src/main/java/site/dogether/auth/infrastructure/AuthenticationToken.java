package site.dogether.auth.infrastructure;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import lombok.Getter;

@Getter
public class AuthenticationToken {

    public static final String PREFIX = "Bearer ";

    private final String value;

    public AuthenticationToken(final String bearerToken) {
        this.value = extract(bearerToken);
    }

    public AuthenticationToken(final Long memberId, final String secret, final Long expireTime) {
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

    private String extract(final String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(PREFIX)) {
            return bearerToken.substring(PREFIX.length());
        }
        return bearerToken;
    }
}
