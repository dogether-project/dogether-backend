package site.dogether.challengegroup.service.dto;

import lombok.Getter;

@Getter
public class ChallengeGroupMemberRankProfileDto {

    private final Long memberId;
    private final String name;
    private final String profileImageUrl;

    public ChallengeGroupMemberRankProfileDto(
            final Long memberId,
            final String name,
            final String profileImageUrl
    ) {
        this.memberId = memberId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }
}
