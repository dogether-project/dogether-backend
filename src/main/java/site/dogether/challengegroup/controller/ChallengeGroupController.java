package site.dogether.challengegroup.controller;

import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.CREATE_CHALLENGE_GROUP;
import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.GET_JOINING_CHALLENGE_GROUPS;
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
import site.dogether.challengegroup.controller.response.CreateChallengeGroupResponse;
import site.dogether.challengegroup.controller.response.GetChallengeGroupMembersRank;
import site.dogether.challengegroup.controller.response.GetJoiningChallengeGroupMyActivitySummaryResponse;
import site.dogether.challengegroup.controller.response.GetJoiningChallengeGroupsResponse;
import site.dogether.challengegroup.controller.response.JoinChallengeGroupResponse;
import site.dogether.challengegroup.service.ChallengeGroupService;
import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupDto;
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

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<JoinChallengeGroupResponse>> joinChallengeGroup(
            @Authenticated final Long memberId,
            @RequestBody final JoinChallengeGroupRequest request
            ) {
        JoinChallengeGroupDto joinChallengeGroupDto = challengeGroupService.joinChallengeGroup(request.joinCode(), memberId);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                JOIN_CHALLENGE_GROUP,
                JoinChallengeGroupResponse.from(joinChallengeGroupDto))
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GetJoiningChallengeGroupsResponse>> getJoiningChallengeGroups(
            @Authenticated final Long memberId
    ) {
        final List<JoiningChallengeGroupDto> joiningChallengeGroups = challengeGroupService.getJoiningChallengeGroups(memberId);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                GET_JOINING_CHALLENGE_GROUPS,
                new GetJoiningChallengeGroupsResponse(joiningChallengeGroups))
        );
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveChallengeGroup(
            @Authenticated final Long memberId,
            @PathVariable final Long groupId
    ) {
        challengeGroupService.leaveChallengeGroup(memberId, groupId);
        return ResponseEntity.ok(ApiResponse.success(
                LEAVE_CHALLENGE_GROUP
        ));
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
}
