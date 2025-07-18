package site.dogether.challengegroup.exception.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.handler.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ChallengeGroupErrorCode implements ErrorCode {

    CHALLENGE_GROUP_ERROR("CGF-0001", "챌린지 그룹 기능에 예기치 못한 문제가 발생했습니다."),
    ALREADY_JOIN_CHALLENGE_GROUP_ERROR("CGF-0002", "이미 가입한 챌린지 그룹에는 참여할 수 없습니다."),
    JOINING_CHALLENGE_GROUP_ALREADY_FULL_MEMBER_ERROR("CGF-0003", "이미 인원이 가득 찬 챌린지 그룹에는 참여할 수 없습니다."),
    JOINING_CHALLENGE_GROUP_ALREADY_FINISHED_ERROR("CGF-0004", "이미 종료된 챌린지 그룹에는 참여할 수 없습니다."),
    JOINING_CHALLENGE_GROUP_NOT_FOUND_ERROR("CGF-0005", "입력한 참여 코드에 대응되는 챌린지 그룹이 존재하지 않습니다."),
    ;

    private final String value;
    private final String message;
}
