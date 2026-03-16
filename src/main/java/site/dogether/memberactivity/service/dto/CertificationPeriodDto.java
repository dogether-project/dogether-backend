package site.dogether.memberactivity.service.dto;

public record CertificationPeriodDto(
    int day,
    int createdCount,
    int certificatedCount,
    int certificationRate
) {
}
