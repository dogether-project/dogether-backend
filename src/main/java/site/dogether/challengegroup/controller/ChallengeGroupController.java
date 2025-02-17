package site.dogether.challengegroup.controller;

import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.CREATE_CHALLENGE_GROUP;
import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.GET_IS_JOINED_CHALLENGE_GROUP;
import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.GET_JOINING_CHALLENGE_GROUP_INFO;
import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.GET_JOINING_CHALLENGE_GROUP_MY_ACTIVITY_SUMMARY;
import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.GET_JOINING_CHALLENGE_GROUP_TEAM_ACTIVITY_SUMMARY;
import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.JOIN_CHALLENGE_GROUP;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.challengegroup.controller.request.CreateChallengeGroupRequest;
import site.dogether.challengegroup.controller.request.JoinChallengeGroupRequest;
import site.dogether.challengegroup.controller.response.CreateChallengeGroupResponse;
import site.dogether.challengegroup.controller.response.GetJoiningChallengeGroupInfoResponse;
import site.dogether.challengegroup.controller.response.GetJoiningChallengeGroupMyActivitySummaryResponse;
import site.dogether.challengegroup.controller.response.GetJoiningChallengeGroupTeamActivitySummaryResponse;
import site.dogether.challengegroup.controller.response.GetJoiningChallengeGroupTeamActivitySummaryResponse.RankResponse;
import site.dogether.challengegroup.service.ChallengeGroupService;
import site.dogether.challengegroup.service.JoiningChallengeGroupTeamActivityDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupInfo;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupMyActivityDto;
import site.dogether.common.config.web.resolver.Authentication;
import site.dogether.common.controller.response.ApiResponse;

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

    @GetMapping("/isJoined")
    public ResponseEntity<ApiResponse<Boolean>> isJoinedChallengeGroup(
            @Authentication final String authenticationToken
    ) {
        final boolean isJoined = challengeGroupService.isJoinedChallengeGroup(authenticationToken);
        return ResponseEntity.ok(
                ApiResponse.successWithData(
                        GET_IS_JOINED_CHALLENGE_GROUP,
                        isJoined
                ));
    }
}
