package site.dogether.appinfo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.appinfo.service.exception.InvalidAppVersionException;
import site.dogether.common.controller.response.ApiResponse;

import static site.dogether.appinfo.controller.response.AppInfoErrorCode.INVALID_APP_VERSION;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class AppInfoExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleInvalidAppVersionException(final InvalidAppVersionException e) {
        log.info("handle InvalidAppVersionException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(INVALID_APP_VERSION, e.getMessage()));
    }
}
