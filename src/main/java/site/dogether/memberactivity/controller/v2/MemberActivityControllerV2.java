package site.dogether.memberactivity.controller.v2;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.dto.response.ApiResponse;
import site.dogether.memberactivity.controller.v2.dto.response.GetMyCertificationStatsApiResponseV2;
import site.dogether.memberactivity.controller.v2.dto.response.GetMyCertificationsApiResponseV2;
import site.dogether.memberactivity.controller.v2.dto.response.GetMyChallengeGroupActivitySummaryApiResponseV2;
import site.dogether.memberactivity.service.MemberActivityService;
import site.dogether.memberactivity.service.dto.CertificationPeriodDto;
import site.dogether.memberactivity.service.dto.ChallengeGroupInfoDto;
import site.dogether.memberactivity.service.dto.GroupedCertificationsResultDto;
import site.dogether.memberactivity.service.dto.MyCertificationStatsDto;
import site.dogether.memberactivity.service.dto.MyRankInChallengeGroupDto;

import java.util.List;

import static site.dogether.common.controller.dto.response.ApiResponse.success;

@RequiredArgsConstructor
@RequestMapping("/api/v2/my")
@RestController
public class MemberActivityControllerV2 {

    private final MemberActivityService memberActivityService;

    @GetMapping("/groups/{groupId}/activity-summary")
    public ResponseEntity<ApiResponse<GetMyChallengeGroupActivitySummaryApiResponseV2>> getMyChallengeGroupActivitySummary(
        @Authenticated final Long memberId, @PathVariable final Long groupId
    ) {
        final ChallengeGroupInfoDto challengeGroupInfo = memberActivityService.getChallengeGroupInfo(memberId, groupId);
        final List<CertificationPeriodDto> certificationPeriods = memberActivityService.getCertificationPeriods(memberId, groupId);
        final MyRankInChallengeGroupDto myRankInChallengeGroup = memberActivityService.getMyRankInChallengeGroup(memberId, groupId);

        return ResponseEntity.ok(success(GetMyChallengeGroupActivitySummaryApiResponseV2.of(
            challengeGroupInfo,
            certificationPeriods,
            myRankInChallengeGroup
        )));
    }

    @GetMapping("/certifications")
    public ResponseEntity<ApiResponse<GetMyCertificationsApiResponseV2>> getMyCertifications(
            @Authenticated final Long memberId,
            @RequestParam final String sortBy,
            @RequestParam(required = false) final String status,
            @PageableDefault(size = 50) final Pageable pageable
    ) {
        final GroupedCertificationsResultDto certifications = memberActivityService.getCertifications(memberId, sortBy, status, pageable);

        return ResponseEntity.ok(success(GetMyCertificationsApiResponseV2.of(certifications)));
    }

    @GetMapping("/certification-stats")
    public ResponseEntity<ApiResponse<GetMyCertificationStatsApiResponseV2>> getMyCertificationStats(
        @Authenticated final Long memberId,
        @RequestParam(required = false) final Long groupId
    ) {
        final MyCertificationStatsDto myCertificationStats = memberActivityService.getMyCertificationStats(memberId, groupId);

        return ResponseEntity.ok(success(GetMyCertificationStatsApiResponseV2.of(myCertificationStats)));
    }
}
