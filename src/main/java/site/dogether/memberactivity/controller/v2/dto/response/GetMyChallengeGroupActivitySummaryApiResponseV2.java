package site.dogether.memberactivity.controller.v2.dto.response;

import site.dogether.memberactivity.service.dto.CertificationPeriodDto;
import site.dogether.memberactivity.service.dto.ChallengeGroupInfoDto;
import site.dogether.memberactivity.service.dto.MyRankInChallengeGroupDto;

import java.util.List;

public record GetMyChallengeGroupActivitySummaryApiResponseV2(
    ChallengeGroupInfo groupInfo,
    List<CertificationPeriod> certificationPeriods,
    MyRankInChallengeGroup ranking
) {

    public static GetMyChallengeGroupActivitySummaryApiResponseV2 of(
        final ChallengeGroupInfoDto groupInfoDto,
        final List<CertificationPeriodDto> periodsDto,
        final MyRankInChallengeGroupDto rankDto
    ) {
        return new GetMyChallengeGroupActivitySummaryApiResponseV2(
            ChallengeGroupInfo.from(groupInfoDto),
            CertificationPeriod.from(periodsDto),
            MyRankInChallengeGroup.from(rankDto)
        );
    }

    public record ChallengeGroupInfo(
        String name,
        int maximumMemberCount,
        int currentMemberCount,
        String joinCode,
        String endAt
    ) {
        public static ChallengeGroupInfo from(final ChallengeGroupInfoDto dto) {
            return new ChallengeGroupInfo(
                dto.name(),
                dto.maximumMemberCount(),
                dto.currentMemberCount(),
                dto.joinCode(),
                dto.endAt()
            );
        }
    }

    public record CertificationPeriod(
        int day,
        int createdCount,
        int certificatedCount,
        int certificationRate
    ) {
        public static List<CertificationPeriod> from(final List<CertificationPeriodDto> dtos) {
            return dtos.stream()
                .map(dto -> new CertificationPeriod(
                    dto.day(),
                    dto.createdCount(),
                    dto.certificatedCount(),
                    dto.certificationRate()
                ))
                .toList();
        }
    }

    public record MyRankInChallengeGroup(
        int totalMemberCount,
        int myRank
    ) {
        public static MyRankInChallengeGroup from(final MyRankInChallengeGroupDto dto) {
            return new MyRankInChallengeGroup(
                dto.totalMemberCount(),
                dto.myRank()
            );
        }
    }
}
