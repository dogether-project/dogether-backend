package site.dogether.dailytodo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.dailytodo.domain.exception.InvalidDailyTodoException;
import site.dogether.dailytodo.service.exception.UnreviewedDailyTodoExistsException;

import static site.dogether.dailytodo.controller.response.DailyTodoExceptionCode.INVALID_DAILY_TODO;
import static site.dogether.dailytodo.controller.response.DailyTodoExceptionCode.UNREVIEWED_DAILY_TODO_EXIST;

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
}
