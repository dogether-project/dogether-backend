package site.dogether.memberactivity.controller.v0.dto.response;

import java.util.List;

public record GetGroupActivityStatResponse(
        ChallengeGroupInfoResponse groupInfo,
        List<CertificationPeriodResponse> certificationPeriods,
        RankingResponse ranking,
        MemberStatsResponse stats
        ) {

        public record ChallengeGroupInfoResponse(String name, int maximumMemberCount, int currentMemberCount, String joinCode, String endAt) {
        }

        public record CertificationPeriodResponse(int day, int createdCount, int certificatedCount, int certificationRate) {
        }

        public record RankingResponse(int totalMemberCount, int myRank) {
        }

        public record MemberStatsResponse(int certificatedCount, int approvedCount, int rejectedCount) {
        }
}
