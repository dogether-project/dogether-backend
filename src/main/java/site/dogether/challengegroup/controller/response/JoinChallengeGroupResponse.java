package site.dogether.challengegroup.controller.response;

public record JoinChallengeGroupResponse(
    String name,
    int maximumMemberCount,
    String startAt,
    int durationOption
) {
}
