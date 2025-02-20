package site.dogether.challengegroup.controller.response;

import java.util.List;
import site.dogether.dailytodo.domain.Rank;

public record GetJoiningChallengeGroupTeamActivitySummaryResponse(
    List<RankResponse> ranking
) {
    public record RankResponse(
        int rank,
        String name,
        int certificationRate
    ) {
        public static List<RankResponse> of(List<Rank> ranks) {
            return ranks.stream()
                .map(rank -> new RankResponse(rank.getRank(), rank.getName(), rank.getCertificationRate()))
                .toList();
        }
    }
}
