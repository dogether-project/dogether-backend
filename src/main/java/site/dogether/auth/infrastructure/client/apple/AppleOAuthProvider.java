package site.dogether.auth.infrastructure.client.apple;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.dogether.auth.infrastructure.JwtHandler;
import site.dogether.auth.infrastructure.client.apple.response.ApplePublicKeySetResponse;
import site.dogether.auth.infrastructure.client.apple.response.ApplePublicKeySetResponse.Key;

@Slf4j
@RequiredArgsConstructor
@Component
public class AppleOAuthProvider {

    private final AppleApiClient appleApiClient;
    private final JwtHandler jwtHandler;
    private final ObjectMapper objectMapper;

    public String getSubjectFromIdToken(String idToken) {
        String headerOfIdToken = idToken.split("\\.")[0];
        String decodedHeader = new String(Base64.getDecoder().decode(headerOfIdToken));
        try {
            Map<String, String> header = objectMapper.readValue(decodedHeader, new TypeReference<>() {});
            PublicKey publicKey = getApplePublicKey(header);
            return jwtHandler.parseClaimsOfIdToken(idToken, publicKey);
        } catch (Exception e) {
            log.error("IdToken 헤더 파싱에 실패하였습니다.", e);
        }
        return null;
    }

    private PublicKey getApplePublicKey(final Map<String, String> header)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ApplePublicKeySetResponse response = appleApiClient.requestPublicKeySet();
        Key key = response.findMatchedPublicKey(header.get("kid"), header.get("alg"));

        byte[] nBytes = Base64.getUrlDecoder().decode(key.n());
        byte[] eBytes = Base64.getUrlDecoder().decode(key.e());
        BigInteger nBigInt = new BigInteger(1, nBytes);
        BigInteger eBigInt = new BigInteger(1, eBytes);

        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(nBigInt, eBigInt);
        KeyFactory keyFactory = KeyFactory.getInstance(key.kty());

        return keyFactory.generatePublic(rsaPublicKeySpec);
    }


}
