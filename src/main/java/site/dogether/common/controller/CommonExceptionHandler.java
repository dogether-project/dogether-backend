package site.dogether.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.response.ApiResponse;

import static org.springframework.http.ResponseEntity.internalServerError;
import static site.dogether.common.error.CommonExceptionCode.INTERNAL_SERVER_APPLICATION_EXCEPTION;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleException(final Exception e) {
        log.error("handle Exception", e);
        return internalServerError().body(ApiResponse.fail(INTERNAL_SERVER_APPLICATION_EXCEPTION));
    }
}
