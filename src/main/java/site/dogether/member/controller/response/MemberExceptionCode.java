package site.dogether.member.controller.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum MemberExceptionCode implements ExceptionCode {

    INVALID_MEMBER_EXCEPTION("MF-0001");

    private final String value;
}
