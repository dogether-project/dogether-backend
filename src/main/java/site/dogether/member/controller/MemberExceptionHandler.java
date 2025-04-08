package site.dogether.member.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.member.controller.response.MemberExceptionCode;
import site.dogether.member.domain.exception.InvalidMemberException;
import site.dogether.member.service.exception.MemberNotFoundException;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleInvalidMemberException(final InvalidMemberException e) {
        log.warn(e.getMessage());

        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(MemberExceptionCode.INVALID_MEMBER,
                    e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleMemberNotFoundException(final MemberNotFoundException e) {
        log.warn(e.getMessage());

        /**
         * TODO : Error Code에 메시지를 넣게 된다면 fail 메서드 오버로딩
         */
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(
                    MemberExceptionCode.MEMBER_NOT_FOUND,
                    MemberExceptionCode.MEMBER_NOT_FOUND.getMessage()));
    }
}
