package site.dogether.auth.infrastructure.client.apple;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import site.dogether.auth.infrastructure.client.apple.response.ApplePublicKeySetResponse;

@Component
public class AppleApiClient {

    public ApplePublicKeySetResponse requestPublicKeySet() {
        return RestClient.create()
                .get()
                .uri("https://appleid.apple.com/auth/keys")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("4xx error");
                })
                .body(ApplePublicKeySetResponse.class);
    }
}
