package site.dogether.auth.controller.v0.dto.response;

import site.dogether.auth.service.dto.response.LoginResponseDto;

public record LoginApiResponseV0(
    String name,
    String accessToken
) {
    public LoginApiResponseV0(final LoginResponseDto loginResponseDto) {
        this(loginResponseDto.name(), loginResponseDto.accessToken());
    }
}
