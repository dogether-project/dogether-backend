package site.dogether.common.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum CommonExceptionCode implements ExceptionCode {
    INTERNAL_SERVER_APPLICATION("IA-0001"),
    ;

    private final String value;
}
