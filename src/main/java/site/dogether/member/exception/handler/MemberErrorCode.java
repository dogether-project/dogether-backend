package site.dogether.member.exception.handler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.handler.ErrorCode;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum MemberErrorCode implements ErrorCode {

    MEMBER_ERROR("MF-0001", "회원 기능에 예기치 못한 문제가 발생했습니다.");

    private final String value;
    private final String message;
}
