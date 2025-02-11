package site.dogether.auth.infrastructure.client.apple.response;

public record AppleTokenResponse(
        String accessToken,
        String tokenType,
        String expiresIn,
        String refreshToken,
        String idToken
) {
}
