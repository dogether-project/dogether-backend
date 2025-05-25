package site.dogether.dailytodocertification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.dailytodocertification.exception.DailyTodoCertificationNotFoundException;
import site.dogether.dailytodocertification.exception.InvalidDailyTodoCertificationException;
import site.dogether.dailytodocertification.exception.InvalidDailyTodoCertificationReviewStatusException;
import site.dogether.dailytodocertification.exception.InvalidDailyTodoCertificationReviewerException;
import site.dogether.dailytodocertification.exception.NotDailyTodoCertificationReviewerException;

import static site.dogether.dailytodocertification.controller.response.DailyTodoCertificationErrorCode.*;

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

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleDailyTodoCertificationNotFoundException(final DailyTodoCertificationNotFoundException e) {
        log.info("handle DailyTodoCertificationNotFoundException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(DAILY_TODO_CERTIFICATION_NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleNotDailyTodoCertificationReviewerException(final NotDailyTodoCertificationReviewerException e) {
        log.info("handle NotDailyTodoCertificationReviewerException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(NOT_DAILY_TODO_CERTIFICATION_REVIEWER, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleInvalidDailyTodoCertificationReviewerException(final InvalidDailyTodoCertificationReviewerException e) {
        log.info("handle InvalidDailyTodoCertificationReviewerException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(INVALID_DAILY_TODO_CERTIFICATION_REVIEWER, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleInvalidDailyTodoCertificationReviewStatusException(final InvalidDailyTodoCertificationReviewStatusException e) {
        log.info("handle InvalidDailyTodoCertificationReviewStatusException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(INVALID_DAILY_TODO_CERTIFICATION_REVIEW_STATUS, e.getMessage()));
    }
}
