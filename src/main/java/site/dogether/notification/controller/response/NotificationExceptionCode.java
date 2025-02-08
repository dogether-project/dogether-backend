package site.dogether.notification.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum NotificationExceptionCode implements ExceptionCode {

    INVALID_NOTIFICATION_TOKEN("NTF-0001")
    ;

    private final String value;
}
