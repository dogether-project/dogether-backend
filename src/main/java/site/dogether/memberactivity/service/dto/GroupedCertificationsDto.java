package site.dogether.memberactivity.service.dto;

import java.util.List;

public record GroupedCertificationsDto(
    String groupedBy,
    List<DailyTodoCertificationInfoDto> certificationInfo
) {
}
