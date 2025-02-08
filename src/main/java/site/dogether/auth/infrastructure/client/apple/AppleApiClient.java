package site.dogether.auth.infrastructure.client.apple;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import site.dogether.auth.infrastructure.client.apple.response.ApplePublicKeySetResponse;
import site.dogether.auth.infrastructure.client.apple.response.AppleTokenResponse;

@Component
public class AppleApiClient {

    @Value("${oauth.apple.client-id}")
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

    public void requestRevoke(final String clientSecret, final String refreshToken) {
        RestClient.create()
                .post()
                .uri("https://appleid.apple.com/auth/revoke")
                .body("client_id=" + clientId
                        + "&client_secret=" + clientSecret
                        + "&token=" + refreshToken
                        + "&token_type_hint=" + "refresh_token")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("Apple Revoke 요청에 실패하였습니다.");
                });
    }
}
