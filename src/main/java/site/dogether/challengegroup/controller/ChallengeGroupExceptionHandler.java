package site.dogether.challengegroup.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.dogether.challengegroup.service.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.service.exception.NotEnoughChallengeGroupMembersException;
import site.dogether.challengegroup.service.exception.NotRunningChallengeGroupException;
import site.dogether.common.controller.response.ApiResponse;

import static site.dogether.challengegroup.controller.response.ChallengeGroupExceptionCode.*;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ChallengeGroupExceptionHandler {

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
}
