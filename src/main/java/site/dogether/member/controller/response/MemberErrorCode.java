package site.dogether.member.controller.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum MemberErrorCode implements ErrorCode {

    INVALID_MEMBER("MF-0001", "유효하지 않은 회원 정보입니다."),
    MEMBER_NOT_FOUND("MF-0002", "존재하지 않는 회원 정보입니다.");

    private final String value;
    private final String message;
}
