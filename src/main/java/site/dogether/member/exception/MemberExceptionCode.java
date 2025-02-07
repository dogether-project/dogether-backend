package site.dogether.member.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum MemberExceptionCode implements ExceptionCode {

    INVALID_MEMBER_EXCEPTION("MF-0001", "유효하지 않은 로그인 정보입니다."),
    MEMBER_NOT_FOUND_EXCEPTION("MF-0002", "해당하는 회원을 찾을 수 없습니다.");

    private final String value;
    private final String message;
}
