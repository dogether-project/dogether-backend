package site.dogether.auth.controller.v1.dto.request;

import site.dogether.auth.constant.LoginType;

public record WithdrawApiRequestV1(String loginType, String authorizationCode) {

    public LoginType getLoginType() {
        return LoginType.valueOf(loginType);
    }
}
