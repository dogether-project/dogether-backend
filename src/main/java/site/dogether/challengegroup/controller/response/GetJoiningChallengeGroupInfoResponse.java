package site.dogether.challengegroup.controller.response;

public record GetJoiningChallengeGroupInfoResponse(
    String name,
    int maximumMemberCount,
    int maximumTodoCount
) {
}
