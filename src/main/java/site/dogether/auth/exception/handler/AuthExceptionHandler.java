package site.dogether.auth.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.auth.exception.AuthException;
import site.dogether.auth.exception.NeedAppleLoginRevokeException;
import site.dogether.common.controller.dto.response.ApiResponse;

import static site.dogether.auth.exception.handler.AuthErrorCode.AUTH_ERROR;
import static site.dogether.auth.exception.handler.AuthErrorCode.NEED_APPLE_LOGIN_REVOKE;
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

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleNeedAppleLoginRevokeException(final NeedAppleLoginRevokeException e) {
        log.error("{} 발생!", e.getClass().getSimpleName(), e);
        return ResponseEntity.badRequest()
            .body(fail(NEED_APPLE_LOGIN_REVOKE));
    }
}
