package site.dogether.challengegroup.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.challengegroup.exception.ChallengeGroupNotFoundException;
import site.dogether.challengegroup.exception.FinishedChallengeGroupException;
import site.dogether.challengegroup.exception.FullMemberInChallengeGroupException;
import site.dogether.challengegroup.exception.InvalidChallengeGroupDurationException;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;
import site.dogether.challengegroup.exception.InvalidChallengeGroupStartAtException;
import site.dogether.challengegroup.exception.JoiningChallengeGroupMaxCountException;
import site.dogether.challengegroup.exception.MemberAlreadyInChallengeGroupException;
import site.dogether.challengegroup.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.exception.NotRunningChallengeGroupException;
import site.dogether.common.controller.response.ApiResponse;

import static site.dogether.challengegroup.controller.response.ChallengeGroupErrorCode.*;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ChallengeGroupExceptionHandler {

    @ExceptionHandler(InvalidChallengeGroupException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidChallengeGroupException(final InvalidChallengeGroupException e) {
        log.info("handle InvalidChallengeGroupException", e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(INVALID_CHALLENGE_GROUP, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleMemberNotInChallengeGroupException(final MemberNotInChallengeGroupException e) {
        log.info("handle MemberNotInChallengeGroupException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(MEMBER_NOT_IN_CHALLENGE_GROUP, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleNotRunningChallengeGroupException(final NotRunningChallengeGroupException e) {
        log.info("handle NotRunningChallengeGroupException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(NOT_RUNNING_CHALLENGE_GROUP, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleJoiningChallengeGroupMaxCountException(final JoiningChallengeGroupMaxCountException e) {
        log.info("handle JoinChallengeGroupMaxCountException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(JOINING_CHALLENGE_GROUP_MAX_COUNT, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleChallengeGroupNotFoundException(final ChallengeGroupNotFoundException e) {
        log.info("handle ChallengeGroupNotFoundException", e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(CHALLENGE_GROUP_NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleMemberAlreadyInChallengeGroupException(final MemberAlreadyInChallengeGroupException e) {
        log.info("handle MemberAlreadyInChallengeGroupException", e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(MEMBER_ALREADY_IN_CHALLENGE_GROUP, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleFullMemberInChallengeGroupException(final FullMemberInChallengeGroupException e) {
        log.info("handle FullMemberInChallengeGroupException", e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(FULL_MEMBER_IN_CHALLENGE_GROUP, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleFinishedChallengeGroupException(final FinishedChallengeGroupException e) {
        log.info("handle FinishedChallengeGroupException", e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(FINISHED_CHALLENGE_GROUP, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleInvalidDurationException(final InvalidChallengeGroupDurationException e) {
        log.info("handle InvalidChallengeGroupDurationException", e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(INVALID_CHALLENGE_GROUP_DURATION, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleInvalidStartAtException(final InvalidChallengeGroupStartAtException e) {
        log.info("handle InvalidChallengeGroupStartAtException", e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(INVALID_CHALLENGE_GROUP_START_AT, e.getMessage()));
    }
}
