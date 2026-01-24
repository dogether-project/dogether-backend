package site.dogether.memberactivity.controller.v2.dto.response;

import org.springframework.data.domain.Slice;
import site.dogether.memberactivity.service.dto.DailyTodoCertificationInfoDto;
import site.dogether.memberactivity.service.dto.GroupedCertificationsDto;
import site.dogether.memberactivity.service.dto.GroupedCertificationsResultDto;

import java.util.List;

public record GetMyCertificationsApiResponseV2(
    List<Certification> certifications,
    PageInfo pageInfo
) {

    public static GetMyCertificationsApiResponseV2 of(
        final GroupedCertificationsResultDto dto
    ) {
        return new GetMyCertificationsApiResponseV2(
            Certification.fromList(dto.certifications()),
            PageInfo.from(dto.page())
        );
    }

    public record Certification(
        String groupedBy,
        List<CertificationInfo> certificationInfo
    ) {
        public static List<Certification> fromList(final List<GroupedCertificationsDto> dtoList) {
            return dtoList.stream()
                .map(Certification::from)
                .toList();
        }

        private static Certification from(final GroupedCertificationsDto dto) {
            return new Certification(
                dto.groupedBy(),
                dto.certificationInfo().stream()
                    .map(CertificationInfo::from)
                    .toList()
            );
        }
    }

    public record CertificationInfo(
        Long id,
        String content,
        String status,
        String certificationContent,
        String certificationMediaUrl,
        String reviewFeedback
    ) {
        public static CertificationInfo from(final DailyTodoCertificationInfoDto dto) {
            return new CertificationInfo(
                dto.id(),
                dto.content(),
                dto.status(),
                dto.certificationContent(),
                dto.certificationMediaUrl(),
                dto.reviewFeedback()
            );
        }
    }

    public record PageInfo(
        int recentPageNumber,
        boolean hasNext
    ) {
        public static PageInfo from(final Slice<?> slice) {
            return new PageInfo(
                slice.getPageable().getPageNumber(),
                slice.hasNext()
            );
        }
    }
}