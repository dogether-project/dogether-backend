package site.dogether.auth.infrastructure;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AppleClientSecretGenerator {

    private final JwtHandler jwtHandler;

    @Value("${oauth.apple.key}")
    private String keyId;
    @Value("${oauth.apple.team-id}")
    private String teamId;
    @Value("${oauth.apple.client-id}")
    private String clientId;
    @Value("${oauth.apple.private-key}")
    private String privateKey;

    public String createClientSecret() throws NoSuchAlgorithmException, InvalidKeySpecException {
        final Date expireDate = Date.from(
                LocalDateTime.now()
                        .plusMinutes(5)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
        return jwtHandler.createClientSecret(
                keyId,
                teamId,
                expireDate,
                "https://appleid.apple.com",
                clientId,
                getPrivateKey()
        );
    }

    private PrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        final byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        final KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePrivate(spec);
    }
}
