package site.dogether.appinfo.exception.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.handler.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AppInfoErrorCode implements ErrorCode {

    APP_INFO_ERROR("AIF-0001", "앱 정보 기능에 예기치 못한 문제가 발생했습니다."),
    ;

    private final String value;
    private final String message;
}
