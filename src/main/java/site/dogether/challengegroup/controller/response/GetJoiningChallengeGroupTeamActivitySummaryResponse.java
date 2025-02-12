package site.dogether.challengegroup.controller.response;

import java.util.List;

public record GetJoiningChallengeGroupTeamActivitySummaryResponse(
    int totalTodoCount,
    int totalCertificatedCount,
    int totalApprovedCount,
    List<RankResponse> ranking
) {
    public record RankResponse(
        int rank,
        String name,
        double certificationRate,
        double approvalRate
    ) {}
}
