package site.dogether.auth.service.dto.request;

import site.dogether.auth.constant.LoginType;

public record LoginRequestDto(
    LoginType loginType,
    String providerId,
    String name
) {}
