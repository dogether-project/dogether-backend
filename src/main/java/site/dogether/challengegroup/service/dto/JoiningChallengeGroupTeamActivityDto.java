package site.dogether.challengegroup.service.dto;

import java.util.List;

public record JoiningChallengeGroupTeamActivityDto(List<Data> ranking) {
    record Data(
            int rank,
            String profileImageUrl,
            String name,
            int achievementRate
    ) {
    }
}
