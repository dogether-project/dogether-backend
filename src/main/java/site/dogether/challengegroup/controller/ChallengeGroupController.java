package site.dogether.challengegroup.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.dogether.challengegroup.controller.request.CreateChallengeGroupRequest;
import site.dogether.challengegroup.controller.request.JoinChallengeGroupRequest;
import site.dogether.challengegroup.controller.response.*;
import site.dogether.challengegroup.controller.response.GetJoiningChallengeGroupTeamActivitySummaryResponse.RankResponse;
import site.dogether.challengegroup.domain.ChallengeGroupStatus;
import site.dogether.challengegroup.service.ChallengeGroupService;
import site.dogether.challengegroup.service.JoiningChallengeGroupTeamActivityDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupInfo;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupMyActivityDto;
import site.dogether.common.config.web.resolver.Authentication;
import site.dogether.common.controller.response.ApiResponse;

import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.*;

@RequiredArgsConstructor
@RequestMapping("/api/groups")
@RestController
public class ChallengeGroupController {

    private final ChallengeGroupService challengeGroupService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateChallengeGroupResponse>> createChallengeGroup(
            @Authentication final String authenticationToken,
            @RequestBody final CreateChallengeGroupRequest request
    ) {
        final String joinCode = challengeGroupService.createChallengeGroup(request, authenticationToken);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                CREATE_CHALLENGE_GROUP,
                new CreateChallengeGroupResponse(joinCode)));
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<Void>> joinChallengeGroup(
            @Authentication final String authenticationToken,
            @RequestBody final JoinChallengeGroupRequest request
    ) {
        challengeGroupService.joinChallengeGroup(request.joinCode(), authenticationToken);
        return ResponseEntity.ok(
            ApiResponse.success(JOIN_CHALLENGE_GROUP));
    }

    @GetMapping("/info/current")
    public ResponseEntity<ApiResponse<GetJoiningChallengeGroupInfoResponse>> getJoiningChallengeGroupInfo(
            @Authentication final String authenticationToken
    ) {
        final JoiningChallengeGroupInfo joiningGroupInfo = challengeGroupService.getJoiningChallengeGroupInfo(authenticationToken);
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
            @Authentication final String authenticationToken
    ) {
        final JoiningChallengeGroupMyActivityDto joiningChallengeGroupMyActivityDto =
                challengeGroupService.getJoiningChallengeGroupMyActivitySummary(authenticationToken);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                GET_JOINING_CHALLENGE_GROUP_MY_ACTIVITY_SUMMARY,
                new GetJoiningChallengeGroupMyActivitySummaryResponse(
                        joiningChallengeGroupMyActivityDto.totalTodoCount(),
                        joiningChallengeGroupMyActivityDto.totalCertificatedCount(),
                        joiningChallengeGroupMyActivityDto.totalApprovedCount(),
                        joiningChallengeGroupMyActivityDto.totalRejectedCount())));
    }

    @GetMapping("/summary/team")
    public ResponseEntity<ApiResponse<GetJoiningChallengeGroupTeamActivitySummaryResponse>> getJoiningChallengeGroupTeamActivitySummary(
            @Authentication final String authenticationToken
    ) {
        final JoiningChallengeGroupTeamActivityDto joiningChallengeGroupTeamActivityDto =
                challengeGroupService.getJoiningChallengeGroupTeamActivitySummary(authenticationToken);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                GET_JOINING_CHALLENGE_GROUP_TEAM_ACTIVITY_SUMMARY,
                new GetJoiningChallengeGroupTeamActivitySummaryResponse(
                        RankResponse.of(joiningChallengeGroupTeamActivityDto.ranking()))));
    }

    @GetMapping("/isJoining")
    public ResponseEntity<ApiResponse<IsJoiningResponse>> isJoinedChallengeGroup(
            @Authentication final String authenticationToken
    ) {
        final boolean isJoined = challengeGroupService.isJoiningChallengeGroup(authenticationToken);
        return ResponseEntity.ok(
                ApiResponse.successWithData(
                        GET_IS_JOINED_CHALLENGE_GROUP,
                        new IsJoiningResponse(isJoined)
                ));
    }

    @DeleteMapping("/leave")
    public ResponseEntity<ApiResponse<Void>> leaveChallengeGroup(
            @Authentication final String authenticationToken
    ) {
        challengeGroupService.leaveChallengeGroup(authenticationToken);
        return ResponseEntity.ok(ApiResponse.success(
                LEAVE_CHALLENGE_GROUP
        ));
    }

    @GetMapping("/my/status")
    public ResponseEntity<ApiResponse<GetMyChallengeGroupStatusResponse>> getMyChallengeGroupStatus(
        @Authentication final String authenticationToken
    ) {
        final ChallengeGroupStatus myChallengeGroupStatus = challengeGroupService.getMyChallengeGroupStatus(authenticationToken);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                ChallengeGroupSuccessCode.GET_MY_CHALLENGE_GROUP_STATUS,
                new GetMyChallengeGroupStatusResponse(myChallengeGroupStatus)
            )
        );
    }
}
