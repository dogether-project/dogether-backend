package site.dogether.challengegroup.service;

import java.util.List;
import site.dogether.dailytodo.domain.Rank;

public record JoiningChallengeGroupTeamActivityDto(
    List<Rank> ranking
) {
}
