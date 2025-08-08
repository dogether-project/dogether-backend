package site.dogether.auth.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.auth.exception.AuthException;
import site.dogether.common.controller.dto.response.ApiResponse;

import static site.dogether.auth.exception.handler.AuthErrorCode.*;
import static site.dogether.common.controller.dto.response.ApiResponse.fail;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleAuthException(final AuthException e) {
        log.error("{} 발생!", e.getClass().getSimpleName(), e);
        return ResponseEntity.badRequest()
            .body(fail(AUTH_ERROR));
    }
}
