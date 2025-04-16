package site.dogether.memberactivity.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record GetMemberAllStatsResponse(
        DailyTodoStats dailyTodoStats,
        List<DailyTodoCertifications> dailyTodoCertifications
) {

    public record DailyTodoStats(
            int totalCertificatedCount,
            int totalApprovedCount,
            int totalRejectedCount
    ) {
    }

    public record DailyTodoCertifications(
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
