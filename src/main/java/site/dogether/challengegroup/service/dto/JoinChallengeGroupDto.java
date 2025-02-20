package site.dogether.challengegroup.service.dto;

public record JoinChallengeGroupDto(
        String name,
        int maximumMemberCount,
        String startAt,
        int durationOption
) {
}
