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
    private String historyReadStatus;
    private int achievementRate;

    public static ChallengeGroupMemberRankResponse from(Long memberId, RankDto rankDto, String profileImageUrl, String historyReadStatus) {
        return ChallengeGroupMemberRankResponse.builder()
                .memberId(memberId)
                .rank(rankDto.getRank())
                .profileImageUrl(profileImageUrl)
                .name(rankDto.getName())
                .historyReadStatus(historyReadStatus)
                .achievementRate(rankDto.getAchievementRate())
                .build();
    }
}
