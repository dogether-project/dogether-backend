package site.dogether.challengegroup.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import site.dogether.challengegroup.entity.RankingCalculator;

@Getter
@Builder
public class RankDto {

    @Setter
    private int rank;
    private final String name;
    private final int achievementRate;

    public static RankDto from(final ChallengeGroupMemberRankInfo rankInfo) {
        return RankDto.builder()
                .rank(0)
                .name(rankInfo.getMemberName())
                .achievementRate(RankingCalculator.calculateAchievementRate(
                        rankInfo.getMyTodoSummary(),
                        rankInfo.getJoinedAt(),
                        rankInfo.getGroupStartAt(),
                        rankInfo.getGroupEndAt()
                ))
                .build();
    }
}
