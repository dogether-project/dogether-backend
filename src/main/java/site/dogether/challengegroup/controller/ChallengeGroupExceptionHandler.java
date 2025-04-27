package site.dogether.challengegroup.controller;

import static site.dogether.challengegroup.controller.response.ChallengeGroupErrorCode.INVALID_CHALLENGE_GROUP;
import static site.dogether.challengegroup.controller.response.ChallengeGroupErrorCode.JOIN_CHALLENGE_GROUP_MAX_COUNT;
import static site.dogether.challengegroup.controller.response.ChallengeGroupErrorCode.MEMBER_NOT_IN_CHALLENGE_GROUP;
import static site.dogether.challengegroup.controller.response.ChallengeGroupErrorCode.NOT_ENOUGH_CHALLENGE_GROUP_MEMBERS;
import static site.dogether.challengegroup.controller.response.ChallengeGroupErrorCode.NOT_RUNNING_CHALLENGE_GROUP;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;
import site.dogether.challengegroup.exception.JoiningChallengeGroupMaxCountException;
import site.dogether.challengegroup.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.exception.NotEnoughChallengeGroupMembersException;
import site.dogether.challengegroup.exception.NotRunningChallengeGroupException;
import site.dogether.common.controller.response.ApiResponse;

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
    public ResponseEntity<ApiResponse<Void>> handleNotEnoughChallengeGroupMembersException(final NotEnoughChallengeGroupMembersException e) {
        log.info("handle NotEnoughChallengeGroupMembersException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(NOT_ENOUGH_CHALLENGE_GROUP_MEMBERS, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleNotRunningChallengeGroupException(final NotRunningChallengeGroupException e) {
        log.info("handle NotRunningChallengeGroupException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(NOT_RUNNING_CHALLENGE_GROUP, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleJoinChallengeGroupMaxCountException(final JoiningChallengeGroupMaxCountException e) {
        log.info("handle JoinChallengeGroupMaxCountException", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(JOIN_CHALLENGE_GROUP_MAX_COUNT, e.getMessage()));
    }

}
