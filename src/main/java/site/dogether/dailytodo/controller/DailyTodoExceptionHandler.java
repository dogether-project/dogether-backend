package site.dogether.dailytodo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.dailytodo.exception.DailyTodoNotFoundException;
import site.dogether.dailytodo.exception.InvalidDailyTodoException;
import site.dogether.dailytodo.exception.InvalidDailyTodoStatusException;
import site.dogether.dailytodo.exception.InvalidReviewResultException;
import site.dogether.dailytodo.exception.NotCertifyPendingDailyTodoException;
import site.dogether.dailytodo.exception.NotCreatedTodayDailyTodoException;
import site.dogether.dailytodo.exception.NotDailyTodoWriterException;
import site.dogether.dailytodo.exception.NotReviewPendingDailyTodoException;
import site.dogether.dailytodohistory.exception.DailyTodoHistoryAlreadyReadException;
import site.dogether.dailytodohistory.exception.DailyTodoHistoryNotFoundException;

import static site.dogether.dailytodo.controller.response.DailyTodoErrorCode.*;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class DailyTodoExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleInvalidDailyTodoException(final InvalidDailyTodoException e) {
        log.info("handle InvalidDailyTodoException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(INVALID_DAILY_TODO, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleDailyTodoCreatedDateException(final NotCreatedTodayDailyTodoException e) {
        log.info("handle DailyTodoCreatedDateException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(DAILY_TODO_CREATED_DATE, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleDailyTodoNotFoundException(final DailyTodoNotFoundException e) {
        log.info("handle DailyTodoNotFoundException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(DAILY_TODO_NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleDailyTodoStatusException(final NotCertifyPendingDailyTodoException e) {
        log.info("handle DailyTodoStatusException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(DAILY_TODO_STATUS, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleNotDailyTodoOwnerException(final NotDailyTodoWriterException e) {
        log.info("handle NotDailyTodoOwnerException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(NOT_DAILY_TODO_OWNER, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleInvalidDailyTodoStatusException(final InvalidDailyTodoStatusException e) {
        log.info("handle InvalidDailyTodoStatusException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(INVALID_DAILY_TODO_STATUS, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleInvalidReviewResultException(final InvalidReviewResultException e) {
        log.info("handle InvalidReviewResultException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(INVALID_REVIEW_RESULT, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleNotReviewPendingDailyTodoException(final NotReviewPendingDailyTodoException e) {
        log.info("handle NotReviewPendingDailyTodoException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(NOT_REVIEW_PENDING_DAILY_TODO, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleDailyTodoHistoryAlreadyReadException(final DailyTodoHistoryAlreadyReadException e) {
        log.info("handle DailyTodoHistoryAlreadyReadException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(DAILY_TODO_HISTORY_ALREADY_READ, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleDailyTodoHistoryNotFoundException(final DailyTodoHistoryNotFoundException e) {
        log.info("handle DailyTodoHistoryNotFoundException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(DAILY_TODO_HISTORY_NOT_FOUND, e.getMessage()));
    }
}
