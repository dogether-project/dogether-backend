package site.dogether.challengegroup.controller;

import static site.dogether.challengegroup.exception.ChallengeGroupExceptionCode.INVALID_CHALLENGE_GROUP_EXCEPTION;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;
import site.dogether.common.controller.response.ApiResponse;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ChallengeGroupExceptionHandler {

    @ExceptionHandler(InvalidChallengeGroupException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidChallengeGroupException(final InvalidChallengeGroupException e) {
        log.info(e.getMessage());

        return ResponseEntity.badRequest()
                .body(ApiResponse.failWithMessage(INVALID_CHALLENGE_GROUP_EXCEPTION, e.getMessage()));
    }

}
