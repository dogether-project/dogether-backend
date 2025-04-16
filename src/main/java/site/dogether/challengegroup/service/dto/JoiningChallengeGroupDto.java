package site.dogether.challengegroup.service.dto;

public record JoiningChallengeGroupDto(
    String groupName,
    String joinCode,
    String endAt,
    int currentDay
) {
}
