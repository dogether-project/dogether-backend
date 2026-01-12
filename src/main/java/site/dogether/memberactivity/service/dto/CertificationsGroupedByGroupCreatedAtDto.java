package site.dogether.memberactivity.service.dto;

import java.util.List;

public record CertificationsGroupedByGroupCreatedAtDto(
    String groupName,
    List<DailyTodoCertificationInfoDto> certificationInfo
) {
}
