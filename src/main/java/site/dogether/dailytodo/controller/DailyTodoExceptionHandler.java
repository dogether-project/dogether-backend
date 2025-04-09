package site.dogether.dailytodo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.dailytodo.exception.*;

import static site.dogether.dailytodo.controller.response.DailyTodoExceptionCode.*;

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
    public ResponseEntity<ApiResponse<Void>> handleUnreviewedDailyTodoExistsException(final UnreviewedDailyTodoExistsException e) {
        log.info("handle UnreviewedDailyTodoExistsException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(UNREVIEWED_DAILY_TODO_EXIST, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleDailyTodoCreatedDateException(final DailyTodoCreatedDateException e) {
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
    public ResponseEntity<ApiResponse<Void>> handleDailyTodoStatusException(final DailyTodoStatusException e) {
        log.info("handle DailyTodoStatusException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(DAILY_TODO_STATUS, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleNotDailyTodoOwnerException(final NotDailyTodoOwnerException e) {
        log.info("handle NotDailyTodoOwnerException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(NOT_DAILY_TODO_OWNER, e.getMessage()));
    }
}
