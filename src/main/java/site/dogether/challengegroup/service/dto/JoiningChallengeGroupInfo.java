package site.dogether.challengegroup.service.dto;

public record JoiningChallengeGroupInfo(
    String name,
    int currentMemberCount,
    int maximumTodoCount
) {
}
