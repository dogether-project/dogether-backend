package site.dogether.common.exception.handler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_SERVER_APPLICATION_ERROR("CF-0001", "서버 애플리케이션에 예기치 못한 문제가 발생했습니다."),
    ;

    private final String value;
    private final String message;
}
