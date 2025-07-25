package site.dogether.memberactivity.controller.v0;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.memberactivity.controller.v0.dto.response.GetGroupActivityStatResponse;
import site.dogether.memberactivity.controller.v0.dto.response.GetMemberAllStatsResponse;
import site.dogether.memberactivity.controller.v0.dto.response.GetMyProfileResponse;
import site.dogether.memberactivity.service.MemberActivityService;
import site.dogether.memberactivity.service.dto.FindMyProfileDto;

import static site.dogether.common.controller.response.ApiResponse.*;

@RequiredArgsConstructor
@RequestMapping("/api/my")
@RestController
public class MemberActivityController {

    private final MemberActivityService memberActivityService;

    @GetMapping("/groups/{groupId}/activity")
    public ResponseEntity<ApiResponse<GetGroupActivityStatResponse>> getGroupActivityStat(
            @Authenticated final Long memberId, @PathVariable final Long groupId
    ) {
        final GetGroupActivityStatResponse groupActivityStat = memberActivityService.getGroupActivityStat(memberId, groupId);

        return ResponseEntity.ok(success(groupActivityStat));
    }

    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<GetMemberAllStatsResponse>> getMemberAllStats(
            @Authenticated final Long memberId,
            @RequestParam final String sort,
            @RequestParam(required = false) final String status
    ) {
        final GetMemberAllStatsResponse memberAllStats = memberActivityService.getMemberAllStats(memberId, sort, status);

        return ResponseEntity.ok(success(memberAllStats));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<GetMyProfileResponse>> getMyProfile(
            @Authenticated final Long memberId
    ) {
        final FindMyProfileDto myProfile = memberActivityService.getMyProfile(memberId);
        final GetMyProfileResponse response = GetMyProfileResponse.from(myProfile);

        return ResponseEntity.ok(success(response));
    }
}
