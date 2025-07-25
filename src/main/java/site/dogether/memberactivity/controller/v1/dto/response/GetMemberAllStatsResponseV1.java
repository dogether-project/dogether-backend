package site.dogether.memberactivity.controller.v1.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Slice;

import java.util.List;

public record GetMemberAllStatsResponseV1(
    DailyTodoStats dailyTodoStats,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<CertificationsGroupedByTodoCompletedAt> certificationsGroupedByTodoCompletedAt,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<CertificationsGroupedByGroupCreatedAt> certificationsGroupedByGroupCreatedAt,

    PageInfoDto pageInfo
) {
    public record DailyTodoStats(
        int totalCertificatedCount,
        int totalApprovedCount,
        int totalRejectedCount
    ) {
    }

    public record CertificationsGroupedByTodoCompletedAt(
        String createdAt,
        List<DailyTodoCertificationInfo> certificationInfo
    ) {
    }

    public record CertificationsGroupedByGroupCreatedAt(
        String groupName,
        List<DailyTodoCertificationInfo> certificationInfo
    ) {
    }

    public record DailyTodoCertificationInfo(
        Long id,
        String content,
        String status,
        String certificationContent,
        String certificationMediaUrl,
        String reviewFeedback
    ) {
    }

    public record PageInfoDto(
        int totalPageCount,
        int recentPageNumber,
        boolean hasNext,
        int pageSize
    ) {
    }

    public static PageInfoDto from(Slice<?> slice) {
        return new PageInfoDto(
            slice.getPageable().getPageSize(),
            slice.getPageable().getPageNumber(),
            slice.hasNext(),
            slice.getSize()
        );
    }
}