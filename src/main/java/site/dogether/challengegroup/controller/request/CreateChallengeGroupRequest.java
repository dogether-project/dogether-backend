package site.dogether.challengegroup.controller.request;

public record CreateChallengeGroupRequest(
    String name,
    int maximumMemberCount,
    String startAt,
    int durationOption,
    int maximumTodoCount
) {
}
