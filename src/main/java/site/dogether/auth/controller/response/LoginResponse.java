package site.dogether.auth.controller.response;

import site.dogether.member.service.dto.AuthenticatedMember;

public record LoginResponse(String name, String accessToken) {
    public LoginResponse(final AuthenticatedMember authenticatedMember) {
        this(authenticatedMember.name(), authenticatedMember.accessToken());
    }
}
