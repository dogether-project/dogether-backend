package site.dogether.auth.constant;

import site.dogether.auth.exception.InvalidLoginTypeException;

public enum LoginType {

    APPLE,
    KAKAO;

    public static LoginType fromString(final String value) {
        try {
            return LoginType.valueOf(value.toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new InvalidLoginTypeException(String.format("유효하지 않은 소셜 로그인 타입 입니다. (%s)", value));
        }
    }
}
