package site.dogether.member.exception;

import site.dogether.member.domain.exception.MemberException;

public class MemberNotFoundException extends MemberException {

    public MemberNotFoundException(String message) {
        super(message);
    }
}
