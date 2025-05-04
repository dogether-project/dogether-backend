package site.dogether.challengegroup.controller.response;

import lombok.Builder;
import lombok.Getter;
import site.dogether.challengegroup.service.dto.ChallengeGroupMemberRankProfileDto;
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

    public static ChallengeGroupMemberRankResponse from(ChallengeGroupMemberRankProfileDto challengeGroupMemberRankProfileDto, RankDto rankDto, String historyReadStatus) {
        return ChallengeGroupMemberRankResponse.builder()
                .memberId(challengeGroupMemberRankProfileDto.getMemberId())
                .rank(rankDto.getRank())
                .profileImageUrl(challengeGroupMemberRankProfileDto.getProfileImageUrl())
                .name(challengeGroupMemberRankProfileDto.getName())
                .historyReadStatus(historyReadStatus)
                .achievementRate(rankDto.getAchievementRate())
                .build();
    }
}
