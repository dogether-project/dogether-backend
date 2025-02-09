package site.dogether.dailytodocertification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.dailytodocertification.domain.exception.InvalidDailyTodoCertificationException;

import static site.dogether.dailytodocertification.controller.response.DailyTodoCertificationExceptionCode.INVALID_DAILY_TODO_CERTIFICATION;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class DailyTodoCertificationExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleInvalidDailyTodoCertificationException(final InvalidDailyTodoCertificationException e) {
        log.info("handle InvalidDailyTodoCertificationException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(INVALID_DAILY_TODO_CERTIFICATION, e.getMessage()));
    }
}
