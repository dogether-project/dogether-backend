package site.dogether.memberactivity.service.dto;

public record MyCertificationStatsDto(
    int certificatedCount,
    int approvedCount,
    int rejectedCount
) {
}
