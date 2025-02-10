package site.dogether.challengegroup.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum ChallengeGroupExceptionCode implements ExceptionCode {

    INVALID_CHALLENGE_GROUP_EXCEPTION("CGF-0001", "유효하지 않은 챌린지 그룹입니다.");

    private final String value;
    private final String message;
}
