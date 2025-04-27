package site.dogether.challengegroup.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.controller.request.CreateChallengeGroupRequest;
import site.dogether.challengegroup.controller.response.ChallengeGroupMemberRankResponse;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;
import site.dogether.challengegroup.exception.JoiningChallengeGroupMaxCountException;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupMyActivityDto;
import site.dogether.dailytodo.entity.GroupTodoSummary;
import site.dogether.dailytodo.entity.MyTodoSummary;
import site.dogether.dailytodo.service.DailyTodoService;
import site.dogether.member.entity.Member;
import site.dogether.member.service.MemberService;
import site.dogether.notification.service.NotificationService;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChallengeGroupService {

    private final ChallengeGroupRepository challengeGroupRepository;
    private final ChallengeGroupMemberRepository challengeGroupMemberRepository;
    private final NotificationService notificationService;
    private final MemberService memberService;
    private final DailyTodoService dailyTodoService;

    @Transactional
    public String createChallengeGroup(final CreateChallengeGroupRequest request, final Long memberId) {
        final Member member = memberService.getMember(memberId);
        validateJoiningGroupMaxCount(member);

        final LocalDate startAt = request.challengeGroupStartAtOption().calculateStartAt();
        final LocalDate endAt = request.challengeGroupDurationOption().calculateEndAt(startAt);
        final ChallengeGroup challengeGroup = ChallengeGroup.create(
            request.groupName(),
            request.maximumMemberCount(),
            startAt,
            endAt
        );

        final ChallengeGroup savedChallengeGroup = challengeGroupRepository.save(challengeGroup);
        challengeGroupMemberRepository.save(new ChallengeGroupMember(savedChallengeGroup, member));

        return challengeGroup.getJoinCode();
    }

    private void validateJoiningGroupMaxCount(final Member member) {
        final int joiningGroupCount = challengeGroupMemberRepository.countNotFinishedGroupByMemberId(member.getId());
        if (joiningGroupCount >= 5) {
            throw new JoiningChallengeGroupMaxCountException(
                    String.format("참여할 수 있는 그룹은 최대 5개입니다. (memberId : %s)", member.getId()));
        }
    }

    @Transactional
    public JoinChallengeGroupDto joinChallengeGroup(final String joinCode, final Long memberId) {
        final Member joinMember = memberService.getMember(memberId);
        validateJoiningGroupMaxCount(joinMember);

        final ChallengeGroup challengeGroup = challengeGroupRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new InvalidChallengeGroupException(
                        String.format("존재하지 않는 그룹입니다. (joinCode : %s", joinCode)));
        isFinishedGroup(challengeGroup);

        if (challengeGroupMemberRepository.existsByChallengeGroupAndMember(challengeGroup, joinMember)) {
            throw new InvalidChallengeGroupException(
                    String.format("이미 참여 중인 그룹입니다. (groupId : %d)", challengeGroup.getId()));
        }

        final int maximumMemberCount = challengeGroup.getMaximumMemberCount();
        final int currentMemberCount = challengeGroupMemberRepository.countByChallengeGroup(challengeGroup);
        if (currentMemberCount >= maximumMemberCount) {
            throw new InvalidChallengeGroupException(
                String.format("그룹 정원 초과입니다. (currentMemberCount : %d, maximumMemberCount : %d)",
                        currentMemberCount, maximumMemberCount));
        }

        final ChallengeGroupMember challengeGroupMember = new ChallengeGroupMember(challengeGroup, joinMember);
        challengeGroupMemberRepository.save(challengeGroupMember);
        sendJoinNotification(challengeGroup, joinMember);

        return JoinChallengeGroupDto.from(challengeGroup);
    }

    private void sendJoinNotification(ChallengeGroup challengeGroup, Member joinMember) {
        final List<ChallengeGroupMember> groupMembers = challengeGroupMemberRepository.findAllByChallengeGroup(challengeGroup);
        for (final ChallengeGroupMember groupMember : groupMembers) {
            final Long groupMemberId = groupMember.getMember().getId();
            if (groupMemberId.equals(joinMember.getId())) {
                notificationService.sendNotification(
                        joinMember.getId(),
                    "새로운 그룹에 참여했습니다.",
                    challengeGroup.getName() + " 그룹에 참여했습니다.",
                    "JOIN"
                );
                continue;
            }
            notificationService.sendNotification(
                groupMemberId,
                "새로운 멤버가 참여했습니다.",
                joinMember.getName() + "님이 " + challengeGroup.getName() + " 그룹에 새로 합류했습니다.",
                "JOIN"
            );
        }
    }

    public List<JoiningChallengeGroupDto> getJoiningChallengeGroups(final Long memberId) {
        final Member member = memberService.getMember(memberId);

        List<ChallengeGroupMember> challengeGroupMembers = challengeGroupMemberRepository.findNotFinishedGroupByMember(member);
        List<ChallengeGroup> joiningGroups = challengeGroupMembers.stream()
                .map(ChallengeGroupMember::getChallengeGroup)
                .toList();

        return joiningGroups.stream()
                .map(joiningGroup -> JoiningChallengeGroupDto.from(
                        joiningGroup,
                        challengeGroupMemberRepository.countByChallengeGroup(joiningGroup)))
                .toList();
    }

    @Transactional
    public void leaveChallengeGroup(final Long memberId, Long groupId) {
        final Member member = memberService.getMember(memberId);
        final ChallengeGroup challengeGroup = challengeGroupRepository.findById(groupId)
                .orElseThrow(() -> new InvalidChallengeGroupException("해당 그룹이 존재하지 않습니다."));

        final ChallengeGroupMember challengeGroupMember = challengeGroupMemberRepository.findByMemberAndChallengeGroup(member, challengeGroup)
                .orElseThrow(() -> new InvalidChallengeGroupException(
                        String.format("해당 그룹에 속해있지 않습니다. (memberId : %d, groupId : %d)", memberId, groupId)));

        challengeGroupMemberRepository.delete(challengeGroupMember);
    }

    private void isFinishedGroup(final ChallengeGroup joiningGroup) {
        if (joiningGroup.isFinished()) {
            throw new InvalidChallengeGroupException(
                    String.format("이미 종료된 그룹입니다. (groupId: %d)", joiningGroup.getId()));
        }
    }

    public JoiningChallengeGroupMyActivityDto getJoiningChallengeGroupMyActivitySummary(final Long memberId) {
        final Member member = memberService.getMember(memberId);

        final ChallengeGroupMember challengeGroupMember =
                challengeGroupMemberRepository.findByMember(member)
                        .orElseThrow(() -> new InvalidChallengeGroupException("그룹에 속해있지 않은 유저입니다."));
        final ChallengeGroup joiningGroup = challengeGroupMember.getChallengeGroup();
        isFinishedGroup(joiningGroup);

        final MyTodoSummary myTodoSummary = dailyTodoService.getMyTodoSummary(member, joiningGroup);

        return new JoiningChallengeGroupMyActivityDto(
                myTodoSummary.calculateTotalTodoCount(),
                myTodoSummary.calculateTotalCertificatedCount(),
                myTodoSummary.calculateTotalApprovedCount(),
                myTodoSummary.calculateTotalRejectedCount()
        );
    }

    public List<ChallengeGroupMemberRankResponse> getJoiningChallengeGroupTeamActivitySummary(final Long groupId) {
        final ChallengeGroup challengeGroup = challengeGroupRepository.findById(groupId)
                .orElseThrow(() -> new InvalidChallengeGroupException("해당 그룹이 존재하지 않습니다."));

        isFinishedGroup(challengeGroup);

        final List<Member> groupMembers = challengeGroupMemberRepository.findAllByChallengeGroup(challengeGroup)
                .stream()
                .map(ChallengeGroupMember::getMember)
                .toList();

        final List<MyTodoSummary> myTodoSummaries = dailyTodoService.getMyTodoSummaries(groupMembers, challengeGroup);
        final GroupTodoSummary groupTodoSummary = new GroupTodoSummary(myTodoSummaries);

        return List.of();
    }
}
