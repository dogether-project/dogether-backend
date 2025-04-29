package site.dogether.challengegroup.controller.response;

import lombok.Builder;
import lombok.Getter;
import site.dogether.challengegroup.service.dto.RankDto;

@Getter
@Builder
public class ChallengeGroupMemberRankResponse {

    private Long memberId;
    private int rank;
    private String profileImageUrl;
    private String name;
    private int achievementRate;

    public static ChallengeGroupMemberRankResponse from(Long memberId, RankDto rankDto, String profileImageUrl) {
        return ChallengeGroupMemberRankResponse.builder()
                .memberId(memberId)
                .rank(rankDto.getRank())
                .profileImageUrl(profileImageUrl)
                .name(rankDto.getName())
                .achievementRate(rankDto.getAchievementRate())
                .build();
    }
}
