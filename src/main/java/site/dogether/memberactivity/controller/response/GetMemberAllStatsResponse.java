package site.dogether.memberactivity.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record GetMemberAllStatsResponse(
        DailyTodoStats dailyTodoStats,
        Object dailyTodoCertifications
) {

    public record DailyTodoStats(
            int totalCertificatedCount,
            int totalApprovedCount,
            int totalRejectedCount
    ) {
    }

    public record CertificationsSortByTodoCompletedAt(
            String createdAt,
            List<DailyTodoCertificationInfo> certificationInfo
    ){
    }

    public record CertificationsSortByGroupCreatedAt(
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
            String rejectReason
    ) {
    }
}
