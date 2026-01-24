package site.dogether.memberactivity.controller.v2.dto.response;

import site.dogether.memberactivity.service.dto.MyCertificationStatsDto;

public record GetMyCertificationStatsApiResponseV2(
    int certificatedCount,
    int approvedCount,
    int rejectedCount
) {

    public static GetMyCertificationStatsApiResponseV2 of(
        final MyCertificationStatsDto dto
        ) {
        return new GetMyCertificationStatsApiResponseV2(
            dto.certificatedCount(),
            dto.approvedCount(),
            dto.rejectedCount()
        );
    }
}
