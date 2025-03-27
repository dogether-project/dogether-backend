package site.dogether.auth.infrastructure.client.apple;

import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import site.dogether.auth.infrastructure.client.apple.response.ApplePublicKeySetResponse;
import site.dogether.auth.infrastructure.client.apple.response.AppleTokenResponse;

@Slf4j
@Component
public class AppleApiClient {

    @Value("${secret.oauth.apple.client-id}")
    private String clientId;

    public ApplePublicKeySetResponse requestPublicKeySet() {
        return RestClient.create()
                .get()
                .uri("https://appleid.apple.com/auth/keys")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("Apple Public KeySet 요청에 실패하였습니다.");
                })
                .body(ApplePublicKeySetResponse.class);
    }

    public String requestRefreshToken(final String clientSecret, final String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", authorizationCode);
        params.add("grant_type", "authorization_code");

        AppleTokenResponse response = RestClient.create()
                .post()
                .uri("https://appleid.apple.com/auth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    String errorBody = res.getBody().toString();
                    throw new RuntimeException("Apple RefreshToken 요청에 실패하였습니다."
                            + " statusCode: " + res.getStatusCode() + ", body: " + errorBody);
                })
                .body(AppleTokenResponse.class);

        return response.refreshToken();
    }

    public boolean requestRevoke(final String clientSecret, final String refreshToken) {
        try {
            HttpStatusCode statusCode = RestClient.create()
                    .post()
                    .uri("https://appleid.apple.com/auth/revoke")
                    .body("client_id=" + clientId
                            + "&client_secret=" + clientSecret
                            + "&token=" + refreshToken
                            + "&token_type_hint=refresh_token")
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode();

            return isRevokeSucceed(statusCode);
        } catch (Exception e) {
            log.warn("Apple Revoke 요청 중 예외가 발생하였습니다: {}", e.getMessage());
            return false;
        }
    }

    private boolean isRevokeSucceed(HttpStatusCode statusCode) {
        if (statusCode.is2xxSuccessful()) {
            log.info("Apple Revoke 요청에 성공하였습니다.");
            return true;
        } else {
            log.warn("Apple Revoke 요청에 실패하였습니다. statusCode: {}", statusCode);
            return false;
        }
    }

}
