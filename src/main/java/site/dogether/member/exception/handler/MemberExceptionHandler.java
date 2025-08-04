package site.dogether.member.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.dto.response.ApiResponse;
import site.dogether.member.exception.MemberException;

import static site.dogether.common.controller.dto.response.ApiResponse.fail;
import static site.dogether.member.exception.handler.MemberErrorCode.MEMBER_ERROR;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleMemberException(final MemberException e) {
        log.error("{} 발생!", e.getClass().getSimpleName(), e);
        return ResponseEntity.badRequest()
            .body(fail(MEMBER_ERROR));
    }
}
