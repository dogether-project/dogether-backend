package site.dogether.dailytodohistory.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.dailytodohistory.exception.DailyTodoHistoryException;

import static site.dogether.common.controller.response.ApiResponse.fail;
import static site.dogether.dailytodohistory.exception.handler.DailyTodoHistoryErrorCode.DAILY_TODO_HISTORY_ERROR;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class DailyTodoHistoryExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleDailyTodoHistoryException(final DailyTodoHistoryException e) {
        log.error("{} 발생!", e.getClass().getSimpleName(), e);
        return ResponseEntity.badRequest()
            .body(fail(DAILY_TODO_HISTORY_ERROR));
    }
}
