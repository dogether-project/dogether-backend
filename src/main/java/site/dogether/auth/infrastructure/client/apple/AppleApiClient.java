package site.dogether.auth.infrastructure.client.apple;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        log.info("Apple RefreshToken 요청을 시작합니다. code: {}", authorizationCode);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", authorizationCode);
        params.add("grant_type", "authorization_code");

        log.debug("Apple RefreshToken 요청 파라미터: client_id={}, code={}", clientId, authorizationCode);

        AppleTokenResponse response = RestClient.create()
                .post()
                .uri("https://appleid.apple.com/auth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    ErrorResponse errorResponse = new ObjectMapper().readValue(res.getBody().readAllBytes(), ErrorResponse.class);
                    log.warn("Apple RefreshToken 요청 실패: statusCode={}, error={}", res.getStatusCode(), errorResponse.error());
                    throw new RuntimeException("Apple RefreshToken 요청에 실패하였습니다. "
                            + "statusCode: " + res.getStatusCode() + ", "
                            + "error: " + errorResponse.error());
                })
                .body(AppleTokenResponse.class);

        if (response == null) {
            log.warn("Apple 응답이 null입니다.");
            throw new RuntimeException("Apple 응답이 null입니다.");
        }

        if (response.refreshToken() == null) {
            log.warn("idToken: {}", response.idToken());
            log.warn("accessToken: {}", response.accessToken());
            log.warn("expireIn: {}", response.expiresIn());
            log.warn("tokenType: {}", response.tokenType());

            log.warn("Apple 응답에 refreshToken이 포함되지 않았습니다. 응답: {}", response);
            throw new RuntimeException("Apple 응답에 refreshToken이 없습니다.");
        }

        log.info("Apple RefreshToken 요청에 성공하였습니다.");
        return response.refreshToken();
    }

    public record ErrorResponse(String error) {}

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
