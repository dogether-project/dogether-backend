package site.dogether.auth.controller.v1.dto.response;

import site.dogether.member.service.dto.AuthenticatedMember;

public record LoginApiResponseV1(String name, String accessToken) {
    public LoginApiResponseV1(final AuthenticatedMember authenticatedMember) {
        this(authenticatedMember.name(), authenticatedMember.accessToken());
    }
}
