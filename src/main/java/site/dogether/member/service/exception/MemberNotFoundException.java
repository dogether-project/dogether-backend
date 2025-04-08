package site.dogether.member.service.exception;

import site.dogether.member.domain.exception.MemberException;

public class MemberNotFoundException extends MemberException {

    public MemberNotFoundException(String message) {
        super(message);
    }
}
