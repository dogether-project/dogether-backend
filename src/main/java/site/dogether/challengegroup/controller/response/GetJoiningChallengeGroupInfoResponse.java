package site.dogether.challengegroup.controller.response;

public record GetJoiningChallengeGroupInfoResponse(
    String name,
    int duration,
    String joinCode,
    int maximumTodoCount,
    String endAt,
    int remainingDays
) {
}
