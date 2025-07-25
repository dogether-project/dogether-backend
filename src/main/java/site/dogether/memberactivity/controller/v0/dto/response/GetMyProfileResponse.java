package site.dogether.memberactivity.controller.v0.dto.response;

import site.dogether.memberactivity.service.dto.FindMyProfileDto;

public record GetMyProfileResponse(
        String name,
        String profileImageUrl
) {
    public static GetMyProfileResponse from(FindMyProfileDto findMyProfileDto) {
        return new GetMyProfileResponse(findMyProfileDto.getName(), findMyProfileDto.getProfileImageUrl());
    }
}
