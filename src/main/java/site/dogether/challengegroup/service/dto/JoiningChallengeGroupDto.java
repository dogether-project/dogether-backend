package site.dogether.challengegroup.service.dto;

public record JoiningChallengeGroupDto(
    Long groupId,
    String groupName,
    int currentMemberCount,
    int maximumMemberCount,
    String joinCode,
    String endAt,
    int currentDay
) {
}
