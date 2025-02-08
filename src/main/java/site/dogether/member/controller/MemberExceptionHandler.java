package site.dogether.member.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.member.domain.exception.InvalidMemberException;

import static site.dogether.member.controller.response.MemberExceptionCode.INVALID_MEMBER_EXCEPTION;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler(InvalidMemberException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidMemberException(final InvalidMemberException e) {
        log.warn(e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(INVALID_MEMBER_EXCEPTION, e.getMessage()));
    }
}
