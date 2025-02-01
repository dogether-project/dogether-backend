package site.dogether.auth.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.controller.response.SuccessCode;

@RequiredArgsConstructor
@Getter
public enum AuthSuccessCode implements SuccessCode {

    LOGIN("AUS-0001", "로그인이 완료되었습니다."),
    WITHDRAW("AUS-0002", "회원 탈퇴가 완료되었습니다.");

    private final String value;
    private final String message;
}
