package site.dogether.memberactivity.controller.v1;

import lombok.RequiredArgsConstructor;
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
import site.dogether.memberactivity.service.dto.FindMyProfileDto;

import static site.dogether.common.controller.dto.response.ApiResponse.success;

@RequiredArgsConstructor
@RequestMapping("/api/v1/my")
@RestController
public class MemberActivityControllerV1 {

    private final MemberActivityService memberActivityService;

    @GetMapping("/groups/{groupId}/activity")
    public ResponseEntity<ApiResponse<GetGroupActivityStatApiResponseV1>> getGroupActivityStat(
            @Authenticated final Long memberId, @PathVariable final Long groupId
    ) {
        final GetGroupActivityStatApiResponseV1 groupActivityStat = memberActivityService.getGroupActivityStat(memberId, groupId);

        return ResponseEntity.ok(success(groupActivityStat));
    }

    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<GetMemberAllStatsApiResponseV1>> getMemberAllStats(
            @Authenticated final Long memberId,
            @RequestParam final String sort,
            @RequestParam(required = false) final String status
    ) {
        final GetMemberAllStatsApiResponseV1 memberAllStats = memberActivityService.getMemberAllStats(memberId, sort, status);

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
