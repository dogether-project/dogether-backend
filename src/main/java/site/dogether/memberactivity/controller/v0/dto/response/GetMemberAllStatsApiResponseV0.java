package site.dogether.memberactivity.controller.v0.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record GetMemberAllStatsApiResponseV0(
        DailyTodoStats dailyTodoStats,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<CertificationsGroupedByTodoCompletedAt> certificationsGroupedByTodoCompletedAt,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<CertificationsGroupedByGroupCreatedAt> certificationsGroupedByGroupCreatedAt
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
    ){
    }

    public record CertificationsGroupedByGroupCreatedAt(
            String groupName,
            List<DailyTodoCertificationInfo> certificationInfo
    ){
    }

    public record DailyTodoCertificationInfo(
            Long id,
            String content,
            String status,
            String certificationContent,
            String certificationMediaUrl,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            String reviewFeedback
    ) {
    }
}
