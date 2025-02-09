package site.dogether.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import site.dogether.common.controller.response.ApiResponse;

import static org.springframework.http.ResponseEntity.internalServerError;
import static site.dogether.common.exception.CommonExceptionCode.INTERNAL_SERVER_APPLICATION;

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(final NoResourceFoundException e) {
        log.warn("handle NoResourceFoundException - {}", e.getResourcePath());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleException(final Exception e) {
        log.error("handle Exception", e);
        return internalServerError()
            .body(ApiResponse.fail(INTERNAL_SERVER_APPLICATION, e.getMessage()));
    }
}
