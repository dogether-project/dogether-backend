package site.dogether.auth.service;

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
        final JwtToken token = new JwtToken(bearerToken);
        try {
            token.validate(secret);
            log.info("토큰 검증에 성공하였습니다.");
        } catch (Exception e) {
            log.info("토큰 검증에 실패하였습니다.");
        }
    }

    public String createToken(Long memberId) {
        final JwtToken token = new JwtToken(memberId, secret, expireTime);
        log.info("토큰을 생성합니다. {}", token.getValue());
        return token.getValue();
    }

    public Long getMemberId(final JwtToken token) {
        return token.getMemberId(secret);
    }

}
