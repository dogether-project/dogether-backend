package site.dogether.challengegroup.controller.v1;

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
import site.dogether.challengegroup.controller.v1.dto.request.CreateChallengeGroupApiRequestV1;
import site.dogether.challengegroup.controller.v1.dto.request.JoinChallengeGroupApiRequestV1;
import site.dogether.challengegroup.controller.v1.dto.request.SaveLastSelectedChallengeGroupInfoApiRequestV1;
import site.dogether.challengegroup.controller.v1.dto.response.CreateChallengeGroupApiResponseV1;
import site.dogether.challengegroup.controller.v1.dto.response.GetChallengeGroupMembersRankApiResponseV1;
import site.dogether.challengegroup.controller.v1.dto.response.GetJoiningChallengeGroupsApiResponseV1;
import site.dogether.challengegroup.controller.v1.dto.response.IsChallengeGroupParticipationRequiredApiResponseV1;
import site.dogether.challengegroup.controller.v1.dto.response.JoinChallengeGroupApiResponseV1;
import site.dogether.challengegroup.service.ChallengeGroupService;
import site.dogether.challengegroup.service.dto.ChallengeGroupMemberOverviewDto;
import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupsWithLastSelectedGroupIndexDto;
import site.dogether.common.controller.dto.response.ApiResponse;

import java.util.List;

import static site.dogether.common.controller.dto.response.ApiResponse.success;

@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
@RestController
public class ChallengeGroupControllerV1 {

    private final ChallengeGroupService challengeGroupService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateChallengeGroupApiResponseV1>> createChallengeGroup(
            @Authenticated final Long memberId,
            @RequestBody final CreateChallengeGroupApiRequestV1 request
    ) {
        final String joinCode = challengeGroupService.createChallengeGroup(request, memberId);
        return ResponseEntity.ok(success(new CreateChallengeGroupApiResponseV1(joinCode)));
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<JoinChallengeGroupApiResponseV1>> joinChallengeGroup(
            @Authenticated final Long memberId,
            @RequestBody final JoinChallengeGroupApiRequestV1 request
    ) {
        final JoinChallengeGroupDto joinChallengeGroupDto = challengeGroupService.joinChallengeGroup(request.joinCode(), memberId);
        return ResponseEntity.ok(success(JoinChallengeGroupApiResponseV1.from(joinChallengeGroupDto)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<GetJoiningChallengeGroupsApiResponseV1>> getJoiningChallengeGroups(
            @Authenticated final Long memberId
    ) {
        final JoiningChallengeGroupsWithLastSelectedGroupIndexDto joiningChallengeGroups = challengeGroupService.getJoiningChallengeGroups(memberId);
        return ResponseEntity.ok(success(new GetJoiningChallengeGroupsApiResponseV1(joiningChallengeGroups.lastSelectedGroupIndex(), joiningChallengeGroups.joiningChallengeGroups())));
    }

    @PostMapping("/last-selected")
    public ResponseEntity<ApiResponse<Void>> saveLastSelectedChallengeGroupInfo(
        @Authenticated final Long memberId,
        @RequestBody final SaveLastSelectedChallengeGroupInfoApiRequestV1 request
    ) {
        challengeGroupService.saveLastSelectedChallengeGroupRecord(memberId, request.groupId());
        return ResponseEntity.ok(success());
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveChallengeGroup(
            @Authenticated final Long memberId,
            @PathVariable final Long groupId
    ) {
        challengeGroupService.leaveChallengeGroup(memberId, groupId);
        return ResponseEntity.ok(success());
    }

    @GetMapping("/participating")
    public ResponseEntity<ApiResponse<IsChallengeGroupParticipationRequiredApiResponseV1>> isChallengeGroupParticipationRequired(
            @Authenticated final Long memberId
    ) {
        IsChallengeGroupParticipationRequiredApiResponseV1 response = challengeGroupService.isChallengeGroupParticipationRequired(memberId);
        return ResponseEntity.ok(success(response));
    }

    @GetMapping("/{groupId}/ranking")
    public ResponseEntity<ApiResponse<GetChallengeGroupMembersRankApiResponseV1>> getJoiningChallengeGroupTeamRanking(
            @Authenticated final Long memberId,
            @PathVariable final Long groupId
    ) {
        final List<ChallengeGroupMemberOverviewDto> challengeGroupMemberOverview = challengeGroupService.getChallengeGroupMemberOverview(memberId, groupId);
        GetChallengeGroupMembersRankApiResponseV1 response = GetChallengeGroupMembersRankApiResponseV1.from(challengeGroupMemberOverview);

        return ResponseEntity.ok(success(response));
    }
}
