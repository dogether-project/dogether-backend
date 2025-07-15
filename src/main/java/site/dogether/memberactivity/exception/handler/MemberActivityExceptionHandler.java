package site.dogether.memberactivity.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.memberactivity.exception.MemberActivityException;

import static site.dogether.common.controller.response.ApiResponse.fail;
import static site.dogether.memberactivity.exception.handler.MemberActivityErrorCode.MEMBER_ACTIVITY_ERROR;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class MemberActivityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleMemberActivityException(final MemberActivityException e) {
        log.error("{} 발생!", e.getClass().getSimpleName(), e);
        return ResponseEntity.badRequest()
            .body(fail(MEMBER_ACTIVITY_ERROR));
    }
}
