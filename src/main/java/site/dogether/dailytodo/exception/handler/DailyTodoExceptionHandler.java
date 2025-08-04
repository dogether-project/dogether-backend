package site.dogether.dailytodo.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.dto.response.ApiResponse;
import site.dogether.dailytodo.exception.DailyTodoException;

import static site.dogether.common.controller.dto.response.ApiResponse.fail;
import static site.dogether.dailytodo.exception.handler.DailyTodoErrorCode.DAILY_TODO_ERROR;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class DailyTodoExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleDailyTodoException(final DailyTodoException e) {
        log.error("{} 발생!", e.getClass().getSimpleName(), e);
        return ResponseEntity.badRequest()
            .body(fail(DAILY_TODO_ERROR));
    }
}
