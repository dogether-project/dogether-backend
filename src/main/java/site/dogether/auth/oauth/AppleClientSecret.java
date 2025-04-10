package site.dogether.auth.oauth;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AppleClientSecret {

    private final JwtHandler jwtHandler;

    @Value("${secret.oauth.apple.key}")
    private String keyId;
    @Value("${secret.oauth.apple.team-id}")
    private String teamId;
    @Value("${secret.oauth.apple.client-id}")
    private String clientId;
    @Value("${secret.oauth.apple.private-key}")
    private String privateKey;

    public String generate() {
        final Date expireTime = Date.from(
                LocalDateTime.now()
                        .plusHours(1)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
        return jwtHandler.createClientSecret(
                keyId,
                teamId,
                expireTime,
                "https://appleid.apple.com",
                clientId,
                getPrivateKey()
        );
    }

    private PrivateKey getPrivateKey() {
        final byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        try {
            final KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePrivate(spec);
        } catch (NoSuchAlgorithmException e) {
            log.warn("존재하지 않는 키 생성 알고리즘입니다.");
            throw new RuntimeException();
        } catch (InvalidKeySpecException e) {
            log.warn("Apple Private Key를 생성할 수 없습니다.");
            throw new RuntimeException();
        }
    }
}
