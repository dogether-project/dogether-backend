package site.dogether.memberactivity.controller.v1.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Slice;
import site.dogether.memberactivity.service.dto.CertificationsGroupedByCertificatedAtDto;
import site.dogether.memberactivity.service.dto.CertificationsGroupedByGroupCreatedAtDto;
import site.dogether.memberactivity.service.dto.DailyTodoCertificationInfoDto;
import site.dogether.memberactivity.service.dto.MyCertificationStatsDto;

import java.util.List;

public record GetMyActivityStatsAndCertificationsApiResponseV1(
    MyCertificationStats dailyTodoStats,

    // TODO: 추후 v2 올릴 때 'TodoCompletedAt'가 아닌 'CertificatedAt'으로 변경 필요
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<CertificationsGroupedByCertificatedAt> certificationsGroupedByTodoCompletedAt,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<CertificationsGroupedByGroupCreatedAt> certificationsGroupedByGroupCreatedAt,

    PageInfo pageInfo
) {

    public record MyCertificationStats(
        int totalCertificatedCount,
        int totalApprovedCount,
        int totalRejectedCount
    ) {
        public static MyCertificationStats from(final MyCertificationStatsDto dto) {
            return new MyCertificationStats(
                dto.totalCertificatedCount(),
                dto.totalApprovedCount(),
                dto.totalRejectedCount()
            );
        }
    }

    public record CertificationsGroupedByCertificatedAt(
        String createdAt,
        List<DailyTodoCertificationInfo> certificationInfo
    ) {
        public static List<CertificationsGroupedByCertificatedAt> fromList(final List<CertificationsGroupedByCertificatedAtDto> dtoList) {
            return dtoList.stream()
                .map(CertificationsGroupedByCertificatedAt::from)
                .toList();
        }

        private static CertificationsGroupedByCertificatedAt from(final CertificationsGroupedByCertificatedAtDto dto) {
            return new CertificationsGroupedByCertificatedAt(
                dto.createdAt(),
                dto.certificationInfo().stream()
                    .map(DailyTodoCertificationInfo::from)
                    .toList()
            );
        }
    }

    public record CertificationsGroupedByGroupCreatedAt(
        String groupName,
        List<DailyTodoCertificationInfo> certificationInfo
    ) {
        public static List<CertificationsGroupedByGroupCreatedAt> fromList(final List<CertificationsGroupedByGroupCreatedAtDto> dtoList) {
            return dtoList.stream()
                .map(CertificationsGroupedByGroupCreatedAt::from)
                .toList();
        }

        private static CertificationsGroupedByGroupCreatedAt from(final CertificationsGroupedByGroupCreatedAtDto dto) {
            return new CertificationsGroupedByGroupCreatedAt(
                dto.groupName(),
                dto.certificationInfo().stream()
                    .map(DailyTodoCertificationInfo::from)
                    .toList()
            );
        }
    }

    public record DailyTodoCertificationInfo(
        Long id,
        String content,
        String status,
        String certificationContent,
        String certificationMediaUrl,
        String reviewFeedback
    ) {
        public static DailyTodoCertificationInfo from(final DailyTodoCertificationInfoDto dto) {
            return new DailyTodoCertificationInfo(
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
        int totalPageCount,
        int recentPageNumber,
        boolean hasNext,
        int pageSize
    ) {
        public static PageInfo from(final Slice<?> slice) {
            return new PageInfo(
                slice.getPageable().getPageSize(),
                slice.getPageable().getPageNumber(),
                slice.hasNext(),
                slice.getSize()
            );
        }
    }
}