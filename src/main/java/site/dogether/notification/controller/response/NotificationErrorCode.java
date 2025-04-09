package site.dogether.notification.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

    INVALID_NOTIFICATION_TOKEN("NTF-0001")
    ;

    private final String value;
}
