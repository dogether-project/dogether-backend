package site.dogether.challengegroup.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum ChallengeGroupExceptionCode implements ExceptionCode {

    MEMBER_NOT_IN_CHALLENGE_GROUP("CGF-0001"),
    NOT_ENOUGH_CHALLENGE_GROUP_MEMBERS("CGF-0002"),
    ;

    private final String value;
}
