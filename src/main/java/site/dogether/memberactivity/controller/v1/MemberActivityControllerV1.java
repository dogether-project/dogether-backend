package site.dogether.memberactivity.controller.v1;

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
import site.dogether.memberactivity.controller.v1.dto.response.GetGroupActivityStatApiResponseV1;
import site.dogether.memberactivity.controller.v1.dto.response.GetMemberAllStatsApiResponseV1;
import site.dogether.memberactivity.controller.v1.dto.response.GetMyProfileApiResponseV1;
import site.dogether.memberactivity.service.MemberActivityService;
import site.dogether.memberactivity.service.MemberActivityServiceV1;
import site.dogether.memberactivity.service.dto.FindMyProfileDto;

import static site.dogether.common.controller.dto.response.ApiResponse.success;

@RequiredArgsConstructor
@RequestMapping("/api/v1/my")
@RestController
public class MemberActivityControllerV1 {

    //TODO: 추후 버저닝이 V0이 사라질 경우 service단은 V1 제거 예정
    private final MemberActivityService memberActivityService;
    private final MemberActivityServiceV1 memberActivityServiceV1;

    @GetMapping("/groups/{groupId}/activity")
    public ResponseEntity<ApiResponse<GetGroupActivityStatApiResponseV1>> getGroupActivityStat(
        @Authenticated final Long memberId, @PathVariable final Long groupId
    ) {
        final GetGroupActivityStatApiResponseV1 groupActivityStat = memberActivityService.getGroupActivityStat(memberId, groupId);

        return ResponseEntity.ok(success(groupActivityStat));
    }

    //TODO: 추후 service단 V1 교체 필요
    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<GetMemberAllStatsApiResponseV1>> getMemberAllStats(
            @Authenticated final Long memberId,
            @RequestParam final String sortBy,
            @RequestParam(required = false) final String status,
            @PageableDefault(size = 50) final Pageable pageable
    ) {
        final GetMemberAllStatsApiResponseV1 memberAllStats = memberActivityServiceV1.getMemberAllStats(memberId, sortBy, status, pageable);

        return ResponseEntity.ok(success(memberAllStats));
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
