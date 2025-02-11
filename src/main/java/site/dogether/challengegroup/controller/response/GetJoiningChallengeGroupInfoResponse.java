package site.dogether.challengegroup.controller.response;

public record GetJoiningChallengeGroupInfoResponse(
    String name,
    int currentMemberCount,
    int maximumTodoCount
) {
}
