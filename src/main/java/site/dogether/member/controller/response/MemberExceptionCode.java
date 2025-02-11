package site.dogether.member.controller.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum MemberExceptionCode implements ExceptionCode {

    INVALID_MEMBER_EXCEPTION("MF-0001"),
    MEMBER_NOT_FOUND_EXCEPTION("MF-0002");

    private final String value;

}
