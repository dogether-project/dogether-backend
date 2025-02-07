package site.dogether.auth.infrastructure.client.apple;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.dogether.auth.infrastructure.JwtHandler;
import site.dogether.auth.infrastructure.client.apple.response.AppleKeySetResponse;

@RequiredArgsConstructor
@Component
public class AppleOAuthProvider {

    private final AppleApiClient appleApiClient;
    private final JwtHandler jwtHandler;

    public String getSubjectFromIdToken(String idToken) {
        AppleKeySetResponse response = appleApiClient.requestPublicKeySet();

        return "subject";
    }
}
