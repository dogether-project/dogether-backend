package site.dogether.appinfo.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AppInfoErrorCode implements ErrorCode {

    INVALID_APP_VERSION("AIF-0001"),
    ;

    private final String value;
}
