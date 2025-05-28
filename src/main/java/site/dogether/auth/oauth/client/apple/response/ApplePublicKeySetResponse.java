package site.dogether.auth.oauth.client.apple.response;

import java.util.List;

public record ApplePublicKeySetResponse(
        List<Key> keys
) {
    public record Key(
            String kty,
            String kid,
            String use,
            String alg,
            String n,
            String e
    ) {
    }

    public Key findMatchedPublicKey(final String kid, final String alg) {
        return keys.stream()
                .filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("매칭되는 public key를 찾을 수 없습니다."));
    }
}
