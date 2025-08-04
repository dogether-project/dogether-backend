package site.dogether.memberactivity.controller.v1.dto.response;

import site.dogether.memberactivity.service.dto.FindMyProfileDto;

public record GetMyProfileApiResponseV1(
        String name,
        String profileImageUrl
) {
    public static GetMyProfileApiResponseV1 from(FindMyProfileDto findMyProfileDto) {
        return new GetMyProfileApiResponseV1(findMyProfileDto.getName(), findMyProfileDto.getProfileImageUrl());
    }
}
