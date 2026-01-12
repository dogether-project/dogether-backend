package site.dogether.memberactivity.service.dto;

public record MyCertificationStatsInChallengeGroupDto(
    int certificatedCount,
    int approvedCount,
    int rejectedCount
) {
}
