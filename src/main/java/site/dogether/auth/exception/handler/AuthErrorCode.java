package site.dogether.auth.exception.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.handler.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    AUTH_ERROR("ATF-0001", "인증 & 인가 기능에 예기치 못한 문제가 발생했습니다."),
    NEED_APPLE_LOGIN_REVOKE("ATF-0002", "애플 로그인 리보크 작업이 필요합니다."),
    INVALID_AUTHENTICATION_TOKEN("ATF-0003", "유효하지 않은 인증 토큰입니다.")
    ;

    private final String value;
    private final String message;
}
