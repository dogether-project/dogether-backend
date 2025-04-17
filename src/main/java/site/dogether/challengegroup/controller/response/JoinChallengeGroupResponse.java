package site.dogether.challengegroup.controller.response;

public record JoinChallengeGroupResponse(
    String groupName,
    int duration,
    int maximumMemberCount,
    String startAt,
    String endAt
) {
}
