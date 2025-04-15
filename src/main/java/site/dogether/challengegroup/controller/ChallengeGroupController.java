package site.dogether.challengegroup.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.challengegroup.controller.request.CreateChallengeGroupRequest;
import site.dogether.challengegroup.controller.request.JoinChallengeGroupRequest;
import site.dogether.challengegroup.controller.response.*;
import site.dogether.challengegroup.controller.response.GetJoiningChallengeGroupTeamActivitySummaryResponse.RankResponse;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.service.ChallengeGroupService;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupTeamActivityDto;
import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupInfo;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupMyActivityDto;
import site.dogether.common.controller.response.ApiResponse;

import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.*;

@RequiredArgsConstructor
@RequestMapping("/api/groups")
@RestController
public class ChallengeGroupController {

    private final ChallengeGroupService challengeGroupService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateChallengeGroupResponse>> createChallengeGroup(
            @Authenticated final Long memberId,
            @RequestBody final CreateChallengeGroupRequest request
    ) {
        final String joinCode = challengeGroupService.createChallengeGroup(request, memberId);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                CREATE_CHALLENGE_GROUP,
                new CreateChallengeGroupResponse(joinCode)));
    }

    @PostMapping("/join")
            public ResponseEntity<ApiResponse<JoinChallengeGroupResponse>> joinChallengeGroup(
            @Authenticated final Long memberId,
            @RequestBody final JoinChallengeGroupRequest request
    ) {
        JoinChallengeGroupDto joinChallengeGroupDto = challengeGroupService.joinChallengeGroup(request.joinCode(), memberId);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                    JOIN_CHALLENGE_GROUP,
                    new JoinChallengeGroupResponse(
                            joinChallengeGroupDto.name(),
                            joinChallengeGroupDto.maximumMemberCount(),
                            joinChallengeGroupDto.startAt(),
                            joinChallengeGroupDto.endAt(),
                            joinChallengeGroupDto.durationOption())
            ));
    }

    @GetMapping("/info/current")
    public ResponseEntity<ApiResponse<GetJoiningChallengeGroupInfoResponse>> getJoiningChallengeGroupInfo(
            @Authenticated final Long memberId
    ) {
        final JoiningChallengeGroupInfo joiningGroupInfo = challengeGroupService.getJoiningChallengeGroupInfo(memberId);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                GET_JOINING_CHALLENGE_GROUP_INFO,
                new GetJoiningChallengeGroupInfoResponse(
                        joiningGroupInfo.name(),
                        joiningGroupInfo.duration(),
                        joiningGroupInfo.joinCode(),
                        joiningGroupInfo.maximumTodoCount(),
                        joiningGroupInfo.endAt(),
                        joiningGroupInfo.remainingDays())));
    }

    @GetMapping("/summary/my")
    public ResponseEntity<ApiResponse<GetJoiningChallengeGroupMyActivitySummaryResponse>> getJoiningChallengeGroupMyActivitySummary(
            @Authenticated final Long memberId
    ) {
        final JoiningChallengeGroupMyActivityDto joiningChallengeGroupMyActivityDto =
                challengeGroupService.getJoiningChallengeGroupMyActivitySummary(memberId);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                GET_JOINING_CHALLENGE_GROUP_MY_ACTIVITY_SUMMARY,
                new GetJoiningChallengeGroupMyActivitySummaryResponse(
                        joiningChallengeGroupMyActivityDto.totalTodoCount(),
                        joiningChallengeGroupMyActivityDto.totalCertificatedCount(),
                        joiningChallengeGroupMyActivityDto.totalApprovedCount(),
                        joiningChallengeGroupMyActivityDto.totalRejectedCount())));
    }

    @GetMapping("/{groupId}/ranking")
    public ResponseEntity<ApiResponse<GetJoiningChallengeGroupTeamActivitySummaryResponse>> getJoiningChallengeGroupTeamRanking(
            @PathVariable Long groupId
    ) {
        final JoiningChallengeGroupTeamActivityDto joiningChallengeGroupTeamActivityDto =
                challengeGroupService.getJoiningChallengeGroupTeamActivitySummary(groupId);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                GET_JOINING_CHALLENGE_GROUP_TEAM_ACTIVITY_SUMMARY,
                new GetJoiningChallengeGroupTeamActivitySummaryResponse(
                        RankResponse.of(joiningChallengeGroupTeamActivityDto.ranking()))));
    }

    @GetMapping("/isJoining")
    public ResponseEntity<ApiResponse<IsJoiningResponse>> isJoinedChallengeGroup(
            @Authenticated final Long memberId
    ) {
        final boolean isJoined = challengeGroupService.isJoiningChallengeGroup(memberId);
        return ResponseEntity.ok(
                ApiResponse.successWithData(
                        GET_IS_JOINED_CHALLENGE_GROUP,
                        new IsJoiningResponse(isJoined)
                ));
    }

    @DeleteMapping("/leave")
    public ResponseEntity<ApiResponse<Void>> leaveChallengeGroup(
            @Authenticated final Long memberId
    ) {
        challengeGroupService.leaveChallengeGroup(memberId);
        return ResponseEntity.ok(ApiResponse.success(
                LEAVE_CHALLENGE_GROUP
        ));
    }

    @GetMapping("/my/status")
    public ResponseEntity<ApiResponse<GetMyChallengeGroupStatusResponse>> getMyChallengeGroupStatus(
        @Authenticated final Long memberId
    ) {
        final ChallengeGroupStatus myChallengeGroupStatus = challengeGroupService.getMyChallengeGroupStatus(memberId);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                ChallengeGroupSuccessCode.GET_MY_CHALLENGE_GROUP_STATUS,
                new GetMyChallengeGroupStatusResponse(myChallengeGroupStatus)
            )
        );
    }
}
