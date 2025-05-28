package site.dogether.challengegroup.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.controller.response.SuccessCode;

@Getter
@RequiredArgsConstructor
public enum ChallengeGroupSuccessCode implements SuccessCode {

    CREATE_CHALLENGE_GROUP("CGS-0001", "챌린지 그룹을 생성하였습니다."),
    JOIN_CHALLENGE_GROUP("CGS-0002", "챌린지 그룹에 참여하였습니다."),
    GET_JOINING_CHALLENGE_GROUPS("CGS-0003", "참여중인 챌린지 그룹 정보를 조회하였습니다."),
    GET_JOINING_CHALLENGE_GROUP_NAMES("CGS-0004", "참여중인 챌린지 그룹 목록을 조회하였습니다."),
    IS_PARTICIPATING_CHALLENGE_GROUP("CGS-0005", "챌린지 그룹 참여 여부를 조회하였습니다."),
    GET_JOINING_CHALLENGE_GROUP_TEAM_ACTIVITY_SUMMARY("CGS-0007", "참여중인 그룹의 팀 전체 누적 활동 통계 정보를 조회하였습니다."),
    LEAVE_CHALLENGE_GROUP("CGS-0008", "챌린지 그룹을 탈퇴하였습니다."),
    SAVE_LAST_SELECTED_CHALLENGE_GROUP_ID("CGS-0009", "사용자가 가장 마지막에 선택한 챌린지 그룹 id를 저장하였습니다."),
    ;

    private final String value;
    private final String message;
}
