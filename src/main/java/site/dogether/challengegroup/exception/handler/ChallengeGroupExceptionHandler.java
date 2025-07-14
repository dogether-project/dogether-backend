package site.dogether.challengegroup.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.challengegroup.exception.AlreadyJoinChallengeGroupException;
import site.dogether.challengegroup.exception.ChallengeGroupException;
import site.dogether.challengegroup.exception.ChallengeGroupNotFoundException;
import site.dogether.challengegroup.exception.FinishedChallengeGroupException;
import site.dogether.challengegroup.exception.FullMemberInChallengeGroupException;
import site.dogether.common.controller.response.ApiResponse;

import static site.dogether.challengegroup.exception.handler.ChallengeGroupErrorCode.*;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ChallengeGroupExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleChallengeGroupException(final ChallengeGroupException e) {
        log.error("{} 발생!", e.getClass().getSimpleName(), e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(CHALLENGE_GROUP_ERROR));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleAlreadyJoinChallengeGroupException(final AlreadyJoinChallengeGroupException e) {
        log.info("{} 발생!", e.getClass().getSimpleName(), e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(ALREADY_JOIN_CHALLENGE_GROUP_ERROR));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleFullMemberInChallengeGroupException(final FullMemberInChallengeGroupException e) {
        log.info("{} 발생!", e.getClass().getSimpleName(), e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(FULL_MEMBER_IN_CHALLENGE_GROUP_ERROR));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleFinishedChallengeGroupException(final FinishedChallengeGroupException e) {
        log.info("{} 발생!", e.getClass().getSimpleName(), e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(FINISHED_CHALLENGE_GROUP_ERROR));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleChallengeGroupNotFoundException(final ChallengeGroupNotFoundException e) {
        log.info("{} 발생!", e.getClass().getSimpleName(), e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(CHALLENGE_GROUP_NOT_FOUND_ERROR));
    }
}
