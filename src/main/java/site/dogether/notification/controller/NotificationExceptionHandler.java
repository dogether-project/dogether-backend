package site.dogether.notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.notification.service.exception.InvalidNotificationTokenException;

import static site.dogether.notification.controller.response.NotificationExceptionCode.INVALID_NOTIFICATION_TOKEN;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class NotificationExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleInvalidNotificationTokenException(final InvalidNotificationTokenException e) {
        log.info("handle InvalidNotificationTokenException", e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(INVALID_NOTIFICATION_TOKEN, e.getMessage()));
    }
}
