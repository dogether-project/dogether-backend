package site.dogether.memberactivity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.memberactivity.controller.response.GetGroupActivityStatResponse;
import site.dogether.memberactivity.controller.response.GetMemberAllStatsResponse;
import site.dogether.memberactivity.service.MemberActivityService;

import static site.dogether.memberactivity.controller.response.MemberActivitySuccessCode.GET_GROUP_ACTIVITY_STAT;
import static site.dogether.memberactivity.controller.response.MemberActivitySuccessCode.GET_MEMBER_ALL_STATS;

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

        return ResponseEntity.ok(ApiResponse.successWithData(GET_GROUP_ACTIVITY_STAT, groupActivityStat));
    }

    // TODO: DailyTodoStats 레코드 명이 entity 명과 동일함. 추후 수정 필요
    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<GetMemberAllStatsResponse>> getMemberAllStats(
            @Authenticated final Long memberId,
            @RequestParam final String sort,
            @RequestParam(required = false) final String status
    ) {
        final GetMemberAllStatsResponse memberAllStats = memberActivityService.getMemberAllStats(memberId, sort, status);

        return ResponseEntity.ok(ApiResponse.successWithData(GET_MEMBER_ALL_STATS, memberAllStats));
    }
}
