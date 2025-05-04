package site.dogether.challengegroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.controller.request.CreateChallengeGroupRequest;
import site.dogether.challengegroup.controller.response.ChallengeGroupMemberRankResponse;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.exception.ChallengeGroupNotFoundException;
import site.dogether.challengegroup.exception.FinishedChallengeGroupException;
import site.dogether.challengegroup.exception.FullMemberInChallengeGroupException;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;
import site.dogether.challengegroup.exception.JoiningChallengeGroupMaxCountException;
import site.dogether.challengegroup.exception.MemberAlreadyInChallengeGroupException;
import site.dogether.challengegroup.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.exception.MemberRankNotFoundException;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.challengegroup.service.dto.ChallengeGroupMemberRankInfo;
import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupDto;
import site.dogether.challengegroup.service.dto.RankDto;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.GroupTodoSummary;
import site.dogether.dailytodo.entity.MyTodoSummary;
import site.dogether.dailytodo.service.DailyTodoService;
import site.dogether.member.entity.Member;
import site.dogether.member.service.MemberService;
import site.dogether.notification.service.NotificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

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
                    String.format("참여할 수 있는 그룹은 최대 5개입니다. (memberId: %d), (joiningGroupCount %d): "
                            , member.getId(), joiningGroupCount));
        }
    }

    @Transactional
    public JoinChallengeGroupDto joinChallengeGroup(final String joinCode, final Long memberId) {
        final Member joinMember = memberService.getMember(memberId);
        validateJoiningGroupMaxCount(joinMember);

        final ChallengeGroup challengeGroup = challengeGroupRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new ChallengeGroupNotFoundException(
                        String.format("존재하지 않는 그룹입니다. (joinCode : %s", joinCode)));
        isFinishedGroup(challengeGroup);

        if (challengeGroupMemberRepository.existsByChallengeGroupAndMember(challengeGroup, joinMember)) {
            throw new MemberAlreadyInChallengeGroupException(
                    String.format("이미 참여 중인 그룹입니다. (memberId: %d), groupId : %d)",
                            memberId, challengeGroup.getId()));
        }

        final int maximumMemberCount = challengeGroup.getMaximumMemberCount();
        final int currentMemberCount = challengeGroupMemberRepository.countByChallengeGroup(challengeGroup);
        if (currentMemberCount >= maximumMemberCount) {
            throw new FullMemberInChallengeGroupException(
                String.format("그룹 정원 초과입니다. (currentMemberCount : %d, maximumMemberCount : %d)",
                        currentMemberCount, maximumMemberCount));
        }

        final ChallengeGroupMember challengeGroupMember = new ChallengeGroupMember(challengeGroup, joinMember);
        challengeGroupMemberRepository.save(challengeGroupMember);
        sendJoinNotification(challengeGroup, joinMember);

        return JoinChallengeGroupDto.from(challengeGroup);
    }

    private void sendJoinNotification(final ChallengeGroup challengeGroup, final Member joinMember) {
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

        final List<ChallengeGroupMember> challengeGroupMembers = challengeGroupMemberRepository.findNotFinishedGroupByMember(member);
        final List<ChallengeGroup> joiningGroups = challengeGroupMembers.stream()
                .map(ChallengeGroupMember::getChallengeGroup)
                .toList();

        return joiningGroups.stream()
                .map(joiningGroup -> JoiningChallengeGroupDto.from(
                        joiningGroup,
                        challengeGroupMemberRepository.countByChallengeGroup(joiningGroup)))
                .toList();
    }

    @Transactional
    public void leaveChallengeGroup(final Long memberId, final Long groupId) {
        final Member member = memberService.getMember(memberId);
        final ChallengeGroup challengeGroup = challengeGroupRepository.findById(groupId)
                .orElseThrow(() -> new ChallengeGroupNotFoundException(
                        String.format("존재하지 않는 그룹입니다. (groupId : %s)", groupId)));

        final ChallengeGroupMember challengeGroupMember = challengeGroupMemberRepository.findByMemberAndChallengeGroup(member, challengeGroup)
                .orElseThrow(() -> new MemberNotInChallengeGroupException(
                        String.format("해당 그룹에 속해있지 않습니다. (memberId : %d, groupId : %d)", memberId, groupId)));

        challengeGroupMemberRepository.delete(challengeGroupMember);
    }

    private void isFinishedGroup(final ChallengeGroup joiningGroup) {
        if (joiningGroup.isFinished()) {
            throw new FinishedChallengeGroupException(
                    String.format("이미 종료된 그룹입니다. (groupId: %d)", joiningGroup.getId()));
        }
    }

    // TODO: historyReadStatus 필드 추가 예정 (history 테이블 생성 후 작업 예정)
    public List<ChallengeGroupMemberRankResponse> getChallengeGroupRanking(final Long groupId) {
        final ChallengeGroup challengeGroup = challengeGroupRepository.findById(groupId)
                .orElseThrow(() -> new InvalidChallengeGroupException("해당 그룹이 존재하지 않습니다."));

        isFinishedGroup(challengeGroup);

        final List<ChallengeGroupMember> groupMembers = challengeGroupMemberRepository.findAllByChallengeGroup(challengeGroup);

        final List<Member> members = groupMembers.stream()
                .map(ChallengeGroupMember::getMember)
                .toList();

        final List<Long> memberIds = getChallengeGroupMemberId(members);
        final List<RankDto> memberRanks = calculateChallengeGroupMembersRank(groupMembers, challengeGroup);
        final List<String> profileImageUrls = getChallengeGroupMemberProfileImages(members);

        return IntStream.range(0, memberRanks.size())
                .mapToObj(i -> ChallengeGroupMemberRankResponse.from(
                        memberIds.get(i),
                        memberRanks.get(i),
                        profileImageUrls.get(i),
                        "READYET"
                ))
                .toList();
    }

    private List<Long> getChallengeGroupMemberId(final List<Member> groupMembers) {
        return groupMembers.stream()
                .map(Member::getId)
                .toList();
    }

    public List<RankDto> calculateChallengeGroupMembersRank(final List<ChallengeGroupMember> groupMembers, final ChallengeGroup challengeGroup) {
        final List<ChallengeGroupMemberRankInfo> membersTodoSummary = getChallengeGroupMembersInfo(groupMembers, challengeGroup);
        final GroupTodoSummary groupTodoSummary = new GroupTodoSummary(membersTodoSummary);

        return groupTodoSummary.getRanks();
    }

    public int getMyRank(final Long memberId, final List<ChallengeGroupMember> groupMembers, final ChallengeGroup challengeGroup) {
        final List<RankDto> memberRanks = calculateChallengeGroupMembersRank(groupMembers, challengeGroup);

        for (int i = 0; i < groupMembers.size(); i++) {
            final ChallengeGroupMember groupMember = groupMembers.get(i);
            final Long currentMemberId = groupMember.getMember().getId();

            if (currentMemberId.equals(memberId)) {
                return memberRanks.get(i).getRank();
            }
        }

        throw new MemberRankNotFoundException("해당 memberId에 대한 랭킹 정보를 찾을 수 없습니다.");
    }

    private List<String> getChallengeGroupMemberProfileImages(final List<Member> groupMembers) {
        return groupMembers.stream()
                .map(Member::getProfileImageUrl)
                .toList();
    }

    public List<ChallengeGroupMemberRankInfo> getChallengeGroupMembersInfo(final List<ChallengeGroupMember> groupMembers, final ChallengeGroup challengeGroup) {
        return groupMembers.stream()
                .map(member -> getChallengeGroupMemberInfo(member, challengeGroup))
                .toList();
    }

    public ChallengeGroupMemberRankInfo getChallengeGroupMemberInfo(final ChallengeGroupMember groupMember, final ChallengeGroup challengeGroup) {
        final Member member = groupMember.getMember();
        final List<DailyTodo> dailyTodos = dailyTodoService.getMemberTodos(challengeGroup, member);

        final MyTodoSummary myTodoSummary = new MyTodoSummary(dailyTodos);

        return new ChallengeGroupMemberRankInfo(
                member.getName(),
                myTodoSummary,
                groupMember.getCreatedAt(),
                challengeGroup.getStartAt(),
                challengeGroup.getEndAt()
        );
    }
}
