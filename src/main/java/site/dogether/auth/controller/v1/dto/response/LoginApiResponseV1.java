package site.dogether.auth.controller.v1.dto.response;

import site.dogether.auth.service.dto.response.LoginResponseDto;

public record LoginApiResponseV1(String name, String accessToken) {
    public LoginApiResponseV1(final LoginResponseDto loginResponseDto) {
        this(loginResponseDto.name(), loginResponseDto.accessToken());
    }
}
