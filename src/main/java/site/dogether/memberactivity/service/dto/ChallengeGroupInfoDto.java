package site.dogether.memberactivity.service.dto;

public record ChallengeGroupInfoDto(
    String name,
    int maximumMemberCount,
    int currentMemberCount,
    String joinCode,
    String endAt
) {
}
