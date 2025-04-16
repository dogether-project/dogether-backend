package site.dogether.challengegroup.controller;

import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.CREATE_CHALLENGE_GROUP;
import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.GET_IS_JOINED_CHALLENGE_GROUP;
import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.GET_JOINING_CHALLENGE_GROUP_INFO;
import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.GET_JOINING_CHALLENGE_GROUP_MY_ACTIVITY_SUMMARY;
import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.JOIN_CHALLENGE_GROUP;
import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.LEAVE_CHALLENGE_GROUP;
import static site.dogether.memberactivity.controller.response.MemberActivitySuccessCode.GET_GROUP_ACTIVITY_STAT;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.challengegroup.controller.request.CreateChallengeGroupRequest;
import site.dogether.challengegroup.controller.request.JoinChallengeGroupRequest;
import site.dogether.challengegroup.controller.response.ChallengeGroupMemberRankResponse;
import site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode;
import site.dogether.challengegroup.controller.response.CreateChallengeGroupResponse;
import site.dogether.challengegroup.controller.response.GetChallengeGroupMembersRank;
import site.dogether.challengegroup.controller.response.GetJoiningChallengeGroupInfoResponse;
import site.dogether.challengegroup.controller.response.GetJoiningChallengeGroupMyActivitySummaryResponse;
import site.dogether.challengegroup.controller.response.GetMyChallengeGroupStatusResponse;
import site.dogether.challengegroup.controller.response.IsJoiningResponse;
import site.dogether.challengegroup.controller.response.JoinChallengeGroupResponse;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.service.ChallengeGroupService;
import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupInfo;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupMyActivityDto;
import site.dogether.common.controller.response.ApiResponse;

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

    @PostMapping("/members/me")
    public ResponseEntity<ApiResponse<JoinChallengeGroupResponse>> joinChallengeGroup(
            @Authenticated final Long memberId,
            @RequestBody final JoinChallengeGroupRequest request
    ) {
        JoinChallengeGroupDto joinChallengeGroupDto = challengeGroupService.joinChallengeGroup(request.joinCode(), memberId);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                    JOIN_CHALLENGE_GROUP,
                    new JoinChallengeGroupResponse(
                            joinChallengeGroupDto.groupName(),
                            joinChallengeGroupDto.duration(),
                            joinChallengeGroupDto.maximumMemberCount(),
                            joinChallengeGroupDto.startAt(),
                            joinChallengeGroupDto.endAt())));
    }

    @GetMapping("/members/me")
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
    public ResponseEntity<ApiResponse<GetChallengeGroupMembersRank>> getJoiningChallengeGroupTeamRanking(
            @PathVariable Long groupId
    ) {
        final List<ChallengeGroupMemberRankResponse> groupMemberRanks = List.of(
                new ChallengeGroupMemberRankResponse(1, "성욱이의 셀카.png", "성욱", 100),
                new ChallengeGroupMemberRankResponse(2, "고양이.png", "영재", 80),
                new ChallengeGroupMemberRankResponse(3, "그로밋.png", "서은", 60)
        );
        GetChallengeGroupMembersRank response = new GetChallengeGroupMembersRank(groupMemberRanks);
        return ResponseEntity.ok(ApiResponse.successWithData(GET_GROUP_ACTIVITY_STAT, response));
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
