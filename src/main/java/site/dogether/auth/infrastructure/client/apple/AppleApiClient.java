package site.dogether.auth.infrastructure.client.apple;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
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
        final AppleTokenResponse response = RestClient.create()
                .post()
                .uri("https://appleid.apple.com/auth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("client_id=" + clientId
                        + "&client_secret=" + clientSecret
                        + "&code=" + authorizationCode
                        + "&grant_type=" + "authorization_code")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("Apple RefreshToken 요청에 실패하였습니다.");
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
