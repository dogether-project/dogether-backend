package site.dogether.challengegroup.controller;

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
import site.dogether.challengegroup.controller.request.SaveLastSelectedChallengeGroupInfoRequest;
import site.dogether.challengegroup.controller.response.CreateChallengeGroupResponse;
import site.dogether.challengegroup.controller.response.GetChallengeGroupMembersRankResponse;
import site.dogether.challengegroup.controller.response.GetJoiningChallengeGroupsResponse;
import site.dogether.challengegroup.controller.response.IsParticipatingChallengeGroupResponse;
import site.dogether.challengegroup.controller.response.JoinChallengeGroupResponse;
import site.dogether.challengegroup.service.ChallengeGroupService;
import site.dogether.challengegroup.service.dto.ChallengeGroupMemberOverviewDto;
import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupsWithLastSelectedGroupIndexDto;
import site.dogether.common.controller.response.ApiResponse;

import java.util.List;

import static site.dogether.challengegroup.controller.response.ChallengeGroupSuccessCode.*;
import static site.dogether.memberactivity.controller.response.MemberActivitySuccessCode.GET_GROUP_ACTIVITY_STAT;

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
        final JoinChallengeGroupDto joinChallengeGroupDto = challengeGroupService.joinChallengeGroup(request.joinCode(), memberId);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                JOIN_CHALLENGE_GROUP,
                JoinChallengeGroupResponse.from(joinChallengeGroupDto)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<GetJoiningChallengeGroupsResponse>> getJoiningChallengeGroups(
            @Authenticated final Long memberId
    ) {
        final JoiningChallengeGroupsWithLastSelectedGroupIndexDto joiningChallengeGroups = challengeGroupService.getJoiningChallengeGroups(memberId);
        return ResponseEntity.ok(
            ApiResponse.successWithData(
                GET_JOINING_CHALLENGE_GROUPS,
                new GetJoiningChallengeGroupsResponse(joiningChallengeGroups.lastSelectedGroupIndex(), joiningChallengeGroups.joiningChallengeGroups()))
        );
    }

    @PostMapping("/last-selected")
    public ResponseEntity<ApiResponse<Void>> saveLastSelectedChallengeGroupInfo(
        @Authenticated final Long memberId,
        @RequestBody final SaveLastSelectedChallengeGroupInfoRequest request
    ) {
        challengeGroupService.saveLastSelectedChallengeGroupRecord(memberId, request.groupId());
        return ResponseEntity.ok(ApiResponse.success(SAVE_LAST_SELECTED_CHALLENGE_GROUP_ID));
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

    @GetMapping("/participating")
    public ResponseEntity<ApiResponse<IsParticipatingChallengeGroupResponse>> isParticipatingChallengeGroup(
            @Authenticated final Long memberId
    ) {
        IsParticipatingChallengeGroupResponse response
                = challengeGroupService.isParticipatingChallengeGroup(memberId);
        return ResponseEntity.ok(ApiResponse.successWithData(
                IS_PARTICIPATING_CHALLENGE_GROUP,
                response
        ));
    }

    @GetMapping("/{groupId}/ranking")
    public ResponseEntity<ApiResponse<GetChallengeGroupMembersRankResponse>> getJoiningChallengeGroupTeamRanking(
            @Authenticated final Long memberId,
            @PathVariable final Long groupId
    ) {
        final List<ChallengeGroupMemberOverviewDto> challengeGroupMemberOverview = challengeGroupService.getChallengeGroupMemberOverview(memberId, groupId);
        GetChallengeGroupMembersRankResponse response = GetChallengeGroupMembersRankResponse.from(challengeGroupMemberOverview);

        return ResponseEntity.ok(ApiResponse.successWithData(GET_GROUP_ACTIVITY_STAT, response));
    }
}
