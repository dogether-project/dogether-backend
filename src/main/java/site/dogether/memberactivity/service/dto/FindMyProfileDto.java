package site.dogether.memberactivity.service.dto;

import lombok.Getter;

@Getter
public class FindMyProfileDto {
    String name;
    String profileImageUrl;

    public FindMyProfileDto(
            final String name,
            final String profileImageUrl
    ) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }
}
