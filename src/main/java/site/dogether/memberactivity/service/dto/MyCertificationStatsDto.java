package site.dogether.memberactivity.service.dto;

public record MyCertificationStatsDto(
    int totalCertificatedCount,
    int totalApprovedCount,
    int totalRejectedCount
) {
}
