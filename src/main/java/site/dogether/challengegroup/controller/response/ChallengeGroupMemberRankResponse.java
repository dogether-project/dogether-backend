package site.dogether.challengegroup.controller.response;

public record ChallengeGroupMemberRankResponse(
        int rank,
        String profileImageUrl,
        String name,
        int achievementRate
) {
}
