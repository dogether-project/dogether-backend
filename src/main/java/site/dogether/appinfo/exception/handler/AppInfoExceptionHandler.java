package site.dogether.appinfo.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.appinfo.exception.AppInfoException;
import site.dogether.common.controller.response.ApiResponse;

import static site.dogether.appinfo.exception.handler.AppInfoErrorCode.APP_INFO_ERROR;
import static site.dogether.common.controller.response.ApiResponse.fail;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class AppInfoExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleAppInfoException(final AppInfoException e) {
        log.error("{} 발생!", e.getClass().getSimpleName(), e);
        return ResponseEntity.badRequest()
            .body(fail(APP_INFO_ERROR));
    }
}
