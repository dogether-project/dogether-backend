package site.dogether.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.notification.service.exception.InvalidNotificationTokenException;

import static site.dogether.notification.controller.response.NotificationExceptionCode.INVALID_NOTIFICATION_TOKEN;

@RestControllerAdvice
public class NotificationExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleInvalidTokenException(final InvalidNotificationTokenException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failWithMessage(INVALID_NOTIFICATION_TOKEN, e.getMessage()));
    }
}
