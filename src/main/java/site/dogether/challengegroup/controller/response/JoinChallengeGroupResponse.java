package site.dogether.challengegroup.controller.response;

public record JoinChallengeGroupResponse(
    String name,
    int maximumMemberCount,
    String startAt,
    String endAt,
    int durationOption
) {
}
