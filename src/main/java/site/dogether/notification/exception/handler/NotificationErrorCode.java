package site.dogether.notification.exception.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.handler.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

    NOTIFICATION_ERROR("NF-0001", "알림 기능에 예기치 못한 문제가 발생했습니다.")
    ;

    private final String value;
    private final String message;
}
