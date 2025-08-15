package site.dogether.auth.controller.v0.dto.request;

import site.dogether.auth.constant.LoginType;
import site.dogether.auth.service.dto.request.LoginRequestDto;

public record LoginApiRequestV0(
    String name,
    String idToken
) {
    public LoginRequestDto toLoginRequestDto() {
        return new LoginRequestDto(
            LoginType.APPLE,
            idToken,
            name
        );
    }
}
