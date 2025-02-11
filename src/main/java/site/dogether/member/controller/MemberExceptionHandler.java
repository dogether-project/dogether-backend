package site.dogether.member.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.member.exception.InvalidMemberException;
import site.dogether.member.exception.MemberExceptionCode;
import site.dogether.member.exception.MemberNotFoundException;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler(InvalidMemberException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidMemberException(final InvalidMemberException e) {
        log.warn(e.getMessage());

        return ResponseEntity.badRequest()
                .body(ApiResponse.failWithMessage(MemberExceptionCode.INVALID_MEMBER_EXCEPTION, e.getMessage()));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberNotFoundException(final MemberNotFoundException e) {
        log.warn(e.getMessage());

        return ResponseEntity.badRequest()
                .body(ApiResponse.failWithMessage(MemberExceptionCode.MEMBER_NOT_FOUND_EXCEPTION, e.getMessage()));
    }
}
