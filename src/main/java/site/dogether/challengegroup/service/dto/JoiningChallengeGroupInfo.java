package site.dogether.challengegroup.service.dto;

public record JoiningChallengeGroupInfo(
    String name,
    int duration,
    String joinCode,
    int maximumTodoCount,
    String endAt,
    int remainingDays
) {
}
