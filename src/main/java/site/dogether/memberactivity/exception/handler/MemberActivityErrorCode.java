package site.dogether.memberactivity.exception.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.handler.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum MemberActivityErrorCode implements ErrorCode {

    MEMBER_ACTIVITY_ERROR("MAF-0001", "회원 활동 기능에 예기치 못한 문제가 발생했습니다."),
    ;

    private final String value;
    private final String message;
}
