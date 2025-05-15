package site.dogether.memberactivity.controller.response;

import site.dogether.memberactivity.service.dto.FindMyProfileDto;

public record GetMyProfileResponse(
        String name,
        String profileImageUrl
) {
    public static GetMyProfileResponse from(FindMyProfileDto findMyProfileDto) {
        return new GetMyProfileResponse(findMyProfileDto.getName(), findMyProfileDto.getProfileImageUrl());
    }
}
