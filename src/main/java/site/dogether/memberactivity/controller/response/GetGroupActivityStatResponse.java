package site.dogether.memberactivity.controller.response;

import java.util.List;

public record GetGroupActivityStatResponse(
        String name,
        String endAt,
        List<CertificationPeriodResponse> certificationPeriods,
        RankingResponse ranking,
        MemberStatsResponse stats
        ) {

        public record CertificationPeriodResponse(int day, int createdCount, int certificatedCount, int certificationRate) {
        }

        public record RankingResponse(int totalMemberCount, int myRank) {
        }

        public record MemberStatsResponse(int certificatedCount, int approvedCount, int rejectedCount) {
        }
}
