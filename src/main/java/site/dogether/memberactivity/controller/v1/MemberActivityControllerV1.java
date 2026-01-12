package site.dogether.memberactivity.controller.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.dto.response.ApiResponse;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.memberactivity.controller.v1.dto.response.GetMyActivityStatsAndCertificationsApiResponseV1;
import site.dogether.memberactivity.controller.v1.dto.response.GetMyChallengeGroupActivityStatsApiResponseV1;
import site.dogether.memberactivity.controller.v1.dto.response.GetMyProfileApiResponseV1;
import site.dogether.memberactivity.service.MemberActivityService;
import site.dogether.memberactivity.service.dto.CertificationPeriodDto;
import site.dogether.memberactivity.service.dto.CertificationsGroupedByCertificatedAtDto;
import site.dogether.memberactivity.service.dto.CertificationsGroupedByGroupCreatedAtDto;
import site.dogether.memberactivity.service.dto.ChallengeGroupInfoDto;
import site.dogether.memberactivity.service.dto.FindMyProfileDto;
import site.dogether.memberactivity.service.dto.MyCertificationStatsDto;
import site.dogether.memberactivity.service.dto.MyCertificationStatsInChallengeGroupDto;
import site.dogether.memberactivity.service.dto.MyRankInChallengeGroupDto;

import java.util.List;

import static site.dogether.common.controller.dto.response.ApiResponse.success;

@RequiredArgsConstructor
@RequestMapping("/api/v1/my")
@RestController
public class MemberActivityControllerV1 {

    private final MemberActivityService memberActivityService;

    @GetMapping("/groups/{groupId}/activity")
    public ResponseEntity<ApiResponse<GetMyChallengeGroupActivityStatsApiResponseV1>> getMyChallengeGroupActivityStats(
        @Authenticated final Long memberId, @PathVariable final Long groupId
    ) {
        final ChallengeGroupInfoDto challengeGroupInfo = memberActivityService.getChallengeGroupInfo(memberId, groupId);
        final List<CertificationPeriodDto> certificationPeriods = memberActivityService.getCertificationPeriods(memberId, groupId);
        final MyRankInChallengeGroupDto myRankInChallengeGroup = memberActivityService.getMyRankInChallengeGroup(memberId, groupId);
        final MyCertificationStatsInChallengeGroupDto myCertificationStatsInChallengeGroup = memberActivityService.getMyCertificationStatsInChallengeGroup(memberId, groupId);

        return ResponseEntity.ok(success(new GetMyChallengeGroupActivityStatsApiResponseV1(
            GetMyChallengeGroupActivityStatsApiResponseV1.ChallengeGroupInfo.from(challengeGroupInfo),
            GetMyChallengeGroupActivityStatsApiResponseV1.CertificationPeriod.from(certificationPeriods),
            GetMyChallengeGroupActivityStatsApiResponseV1.MyRankInChallengeGroup.from(myRankInChallengeGroup),
            GetMyChallengeGroupActivityStatsApiResponseV1.MyCertificationStatsInChallengeGroup.from(myCertificationStatsInChallengeGroup)
        )));
    }

    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<GetMyActivityStatsAndCertificationsApiResponseV1>> getMyActivityStatsAndCertifications(
            @Authenticated final Long memberId,
            @RequestParam final String sortBy,
            @RequestParam(required = false) final String status,
            @PageableDefault(size = 50) final Pageable pageable
    ) {
        final MyCertificationStatsDto myCertificationStats = memberActivityService.getMyCertificationStats(memberId);
        final Slice<DailyTodoCertification> certifications = memberActivityService.getCertificationsByStatus(memberId, status, pageable);

        // TODO: 추후 v2 올릴 때 'TODO_COMPLETED_AT'가 아닌 'CERTIFICATED_AT'으로 변경 필요
        if (sortBy.equals("TODO_COMPLETED_AT")) {
            final List<CertificationsGroupedByCertificatedAtDto> groupedCertifications = memberActivityService.certificationsGroupedByCertificatedAt(certifications.getContent());

            return ResponseEntity.ok(success(new GetMyActivityStatsAndCertificationsApiResponseV1(
                GetMyActivityStatsAndCertificationsApiResponseV1.MyCertificationStats.from(myCertificationStats),
                GetMyActivityStatsAndCertificationsApiResponseV1.CertificationsGroupedByCertificatedAt.fromList(groupedCertifications),
                null,
                GetMyActivityStatsAndCertificationsApiResponseV1.PageInfo.from(certifications)
            )));
        }

        // sortBy = GROUP_CREATED_AT
        final List<CertificationsGroupedByGroupCreatedAtDto> groupedCertifications = memberActivityService.certificationsGroupedByGroupCreatedAt(certifications.getContent());

        return ResponseEntity.ok(success(new GetMyActivityStatsAndCertificationsApiResponseV1(
            GetMyActivityStatsAndCertificationsApiResponseV1.MyCertificationStats.from(myCertificationStats),
            null,
            GetMyActivityStatsAndCertificationsApiResponseV1.CertificationsGroupedByGroupCreatedAt.fromList(groupedCertifications),
            GetMyActivityStatsAndCertificationsApiResponseV1.PageInfo.from(certifications)
        )));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<GetMyProfileApiResponseV1>> getMyProfile(
        @Authenticated final Long memberId
    ) {
        final FindMyProfileDto myProfile = memberActivityService.getMyProfile(memberId);
        final GetMyProfileApiResponseV1 response = GetMyProfileApiResponseV1.from(myProfile);

        return ResponseEntity.ok(success(response));
    }
}
