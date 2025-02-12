package site.dogether.challengegroup.service;

import java.util.List;
import site.dogether.dailytodo.domain.Rank;

public record JoiningChallengeGroupTeamActivityDto(
    int totalTodoCount,
    int totalCertificatedCount,
    int totalApprovedCount,
    List<Rank> ranking
) {
}
