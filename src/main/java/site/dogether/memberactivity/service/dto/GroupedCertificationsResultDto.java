package site.dogether.memberactivity.service.dto;

import org.springframework.data.domain.Slice;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;

import java.util.List;

public record GroupedCertificationsResultDto(
    List<GroupedCertificationsDto> certifications,
    Slice<DailyTodoCertification> page
) {
}
