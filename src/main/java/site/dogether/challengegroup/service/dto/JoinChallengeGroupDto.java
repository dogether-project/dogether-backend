package site.dogether.challengegroup.service.dto;

public record JoinChallengeGroupDto(
        String groupName,
        int duration,
        int maximumMemberCount,
        String startAt,
        String endAt
) {
}
