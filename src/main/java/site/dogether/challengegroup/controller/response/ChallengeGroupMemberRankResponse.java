package site.dogether.challengegroup.controller.response;

import lombok.Builder;
import lombok.Getter;
import site.dogether.challengegroup.service.dto.RankDto;

@Getter
@Builder
public class ChallengeGroupMemberRankResponse {

    private int rank;
    private String profileImageUrl;
    private String name;
    private int achievementRate;

    public static ChallengeGroupMemberRankResponse from(RankDto rankDto, String profileImageUrl) {
        return ChallengeGroupMemberRankResponse.builder()
                .rank(rankDto.getRank())
                .profileImageUrl(profileImageUrl)
                .name(rankDto.getName())
                .achievementRate(rankDto.getAchievementRate())
                .build();
    }
}
