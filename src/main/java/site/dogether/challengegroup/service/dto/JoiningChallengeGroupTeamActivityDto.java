package site.dogether.challengegroup.service.dto;

import site.dogether.dailytodo.entity.Rank;

import java.util.List;

public record JoiningChallengeGroupTeamActivityDto(
    List<Rank> ranking
) {
}
