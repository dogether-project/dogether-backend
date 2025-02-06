package site.dogether.member.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum MemberExceptionCode implements ExceptionCode {

    INVALID_MEMBER_EXCEPTION("MF-0001", "유효하지 않은 회원입니다.");

    private final String value;
    private final String message;
}
