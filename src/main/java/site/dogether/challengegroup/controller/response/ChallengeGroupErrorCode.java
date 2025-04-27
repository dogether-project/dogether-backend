package site.dogether.challengegroup.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ChallengeGroupErrorCode implements ErrorCode {

    MEMBER_NOT_IN_CHALLENGE_GROUP("CGF-0001"),
    NOT_ENOUGH_CHALLENGE_GROUP_MEMBERS("CGF-0002"),
    NOT_RUNNING_CHALLENGE_GROUP("CGF-0003"),
    INVALID_CHALLENGE_GROUP("CGF-0004"),
    JOINING_CHALLENGE_GROUP_MAX_COUNT("CGF-0005"),
    CHALLENGE_GROUP_NOT_FOUND("CGF-0006"),
    MEMBER_ALREADY_IN_CHALLENGE_GROUP("CGF-0007"),
    FULL_MEMBER_IN_CHALLENGE_GROUP("CGF-0008"),
    FINISHED_CHALLENGE_GROUP("CGF-0009"),
    ;

    private final String value;
}
