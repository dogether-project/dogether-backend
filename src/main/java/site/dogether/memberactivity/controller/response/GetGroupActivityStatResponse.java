package site.dogether.memberactivity.controller.response;

import java.util.List;

public record GetGroupActivityStatResponse(
        String name,
        String endAt,
        List<CertificationPeriodResponse> certificationPeriods,
        RankingResponse ranking,
        MemberStatsResponse stats
        ) {
}
