package site.dogether.auth.controller.v1.dto.request;

import site.dogether.auth.constant.LoginType;
import site.dogether.auth.service.dto.request.LoginRequestDto;

public record LoginApiRequestV1(
    String loginType,
    String providerId,
    String name
) {
    public LoginRequestDto toLoginRequestDto() {
        return new LoginRequestDto(
            LoginType.fromString(loginType),
            providerId,
            name
        );
    }
}
