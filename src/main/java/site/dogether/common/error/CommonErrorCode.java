package site.dogether.common.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum CommonErrorCode implements ErrorCode {
    ;

    private final String value;
    private final String message;
}
