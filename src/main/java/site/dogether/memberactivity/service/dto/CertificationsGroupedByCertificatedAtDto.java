package site.dogether.memberactivity.service.dto;

import java.util.List;

public record CertificationsGroupedByCertificatedAtDto(
    String createdAt,
    List<DailyTodoCertificationInfoDto> certificationInfo
) {
}
