package site.dogether.auth.infrastructure.client.apple.response;

import java.util.List;

public record AppleKeySetResponse(
        List<Key> keys
) {
    record Key(
            String kty,
            String kid,
            String use,
            String alg,
            String n,
            String e
    ) {
    }

    public Key getMatchedPublicKey(final String kid, final String alg) {
        return keys.stream()
                .filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("매칭되는 public key를 찾을 수 없습니다."));
    }
}
