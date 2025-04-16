package site.dogether.memberactivity.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.controller.response.SuccessCode;

@Getter
@RequiredArgsConstructor
public enum MemberActivitySuccessCode implements SuccessCode {

    GET_All_GROUP_NAMES("CGS-0006", "참여중인 그룹의 이름 목록이 조회되었습니다."),
    GET_GROUP_ACTIVITY_STAT("CGS-0007", "특정 챌린지 그룹의 활동 정보가 조회되었습니다."),
    GET_MEMBER_ALL_STATS("CGS-0008", "사용자의 모든 인증 목록이 조회되었습니다.");

    private final String value;
    private final String message;
}
