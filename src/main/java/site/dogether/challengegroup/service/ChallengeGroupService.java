package site.dogether.challengegroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.controller.request.CreateChallengeGroupRequest;
import site.dogether.challengegroup.controller.response.IsParticipatingChallengeGroupResponse;
import site.dogether.challengegroup.entity.AchievementRateCalculator;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.entity.LastSelectedChallengeGroupRecord;
import site.dogether.challengegroup.exception.AlreadyJoinChallengeGroupException;
import site.dogether.challengegroup.exception.JoiningChallengeGroupNotFoundException;
import site.dogether.challengegroup.exception.ChallengeGroupNotFoundException;
import site.dogether.challengegroup.exception.FinishedChallengeGroupException;
import site.dogether.challengegroup.exception.JoiningChallengeGroupAlreadyFinishedException;
import site.dogether.challengegroup.exception.JoiningChallengeGroupAlreadyFullMemberException;
import site.dogether.challengegroup.exception.JoiningChallengeGroupMaxCountException;
import site.dogether.challengegroup.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.challengegroup.repository.LastSelectedChallengeGroupRecordRepository;
import site.dogether.challengegroup.service.dto.ChallengeGroupMemberOverviewDto;
import site.dogether.challengegroup.service.dto.ChallengeGroupMemberWithAchievementRateDto;
import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupsWithLastSelectedGroupIndexDto;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.service.DailyTodoService;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationCount;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationRepository;
import site.dogether.dailytodohistory.service.DailyTodoHistoryService;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;
import site.dogether.notification.service.NotificationService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChallengeGroupService {

    private final MemberRepository memberRepository;
    private final ChallengeGroupRepository challengeGroupRepository;
    private final ChallengeGroupMemberRepository challengeGroupMemberRepository;
    private final LastSelectedChallengeGroupRecordRepository lastSelectedChallengeGroupRecordRepository;
    private final DailyTodoCertificationRepository dailyTodoCertificationRepository;
    private final DailyTodoService dailyTodoService;
    private final DailyTodoHistoryService dailyTodoHistoryService;
    private final NotificationService notificationService;

    @Transactional
    public String createChallengeGroup(final CreateChallengeGroupRequest request, final Long memberId) {
        final Member member = getMember(memberId);
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
        saveLastSelectedChallengeGroupRecord(member, savedChallengeGroup);

        return challengeGroup.getJoinCode();
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 id입니다. (%d)", memberId)));
    }

    private void validateJoiningGroupMaxCount(final Member member) {
        final int joiningGroupCount = challengeGroupMemberRepository.countNotFinishedGroupByMemberId(member.getId());
        if (joiningGroupCount >= 5) {
            throw new JoiningChallengeGroupMaxCountException(
                String.format("참여할 수 있는 그룹은 최대 5개입니다. (memberId: %d), (joiningGroupCount %d): "
                    , member.getId(), joiningGroupCount));
        }
    }

    private void saveLastSelectedChallengeGroupRecord(final Member member, final ChallengeGroup challengeGroup) {
        lastSelectedChallengeGroupRecordRepository.findByMember(member)
            .ifPresentOrElse(
                lastSelectedChallengeGroupRecord -> lastSelectedChallengeGroupRecord.updateChallengeGroup(challengeGroup),
                () -> lastSelectedChallengeGroupRecordRepository.save(new LastSelectedChallengeGroupRecord(member, challengeGroup)));
    }

    @Transactional
    public JoinChallengeGroupDto joinChallengeGroup(final String joinCode, final Long memberId) {
        final Member joinMember = getMember(memberId);
        validateJoiningGroupMaxCount(joinMember);

        final ChallengeGroup challengeGroup = getJoiningChallengeGroup(joinCode);

        validateJoiningGroupIsNotFinished(challengeGroup);
        memberAlreadyInSameGroup(challengeGroup, joinMember);
        validateJoiningChallengeGroupAlreadyFullMember(challengeGroup);

        final ChallengeGroupMember challengeGroupMember = new ChallengeGroupMember(challengeGroup, joinMember);
        challengeGroupMemberRepository.save(challengeGroupMember);
        saveLastSelectedChallengeGroupRecord(joinMember, challengeGroup);

        sendJoinNotification(challengeGroup, joinMember);

        return JoinChallengeGroupDto.from(challengeGroup);
    }

    private ChallengeGroup getJoiningChallengeGroup(final String joinCode) {
        return challengeGroupRepository.findByJoinCode(joinCode)
            .orElseThrow(() -> new JoiningChallengeGroupNotFoundException(String.format("참여 하려는 챌린지 그룹이 존재하지 않습니다. (%s)", joinCode)));
    }

    private void validateJoiningGroupIsNotFinished(final ChallengeGroup challengeGroup) {
        if (challengeGroup.isFinished()) {
            throw new JoiningChallengeGroupAlreadyFinishedException(
                String.format("이미 종료된 그룹의 초대 코드입니다. (%s)", challengeGroup));
        }
    }

    private void validateJoiningChallengeGroupAlreadyFullMember(ChallengeGroup challengeGroup) {
        final int maximumMemberCount = challengeGroup.getMaximumMemberCount();
        final int currentMemberCount = challengeGroupMemberRepository.countByChallengeGroup(challengeGroup);
        if (currentMemberCount >= maximumMemberCount) {
            throw new JoiningChallengeGroupAlreadyFullMemberException(
                String.format("그룹 정원 초과입니다. (currentMemberCount : %d, maximumMemberCount : %d)",
                    currentMemberCount, maximumMemberCount));
        }
    }

    private void memberAlreadyInSameGroup(ChallengeGroup challengeGroup, Member joinMember) {
        if (challengeGroupMemberRepository.existsByChallengeGroupAndMember(challengeGroup, joinMember)) {
            throw new AlreadyJoinChallengeGroupException(
                    String.format("이미 참여 중인 그룹입니다. (memberId: %d), groupId : %d)",
                            joinMember.getId(), challengeGroup.getId()));
        }
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

    public JoiningChallengeGroupsWithLastSelectedGroupIndexDto getJoiningChallengeGroups(final Long memberId) {
        final Member member = getMember(memberId);

        final List<ChallengeGroupMember> challengeGroupMembers = challengeGroupMemberRepository.findNotFinishedGroupByMember(member);
        final List<ChallengeGroup> joiningGroups = challengeGroupMembers.stream()
            .map(ChallengeGroupMember::getChallengeGroup)
            .toList();

        final List<JoiningChallengeGroupDto> joiningChallengeGroupDtos = joiningGroups.stream()
            .map(joiningGroup -> JoiningChallengeGroupDto.from(
                joiningGroup,
                challengeGroupMemberRepository.countByChallengeGroup(joiningGroup)))
            .toList();

        final Integer lastSelectedGroupIndex = findLastSelectedChallengeGroupIndexByMember(member, joiningGroups);
        return new JoiningChallengeGroupsWithLastSelectedGroupIndexDto(lastSelectedGroupIndex, joiningChallengeGroupDtos);
    }

    private Integer findLastSelectedChallengeGroupIndexByMember(final Member member, final List<ChallengeGroup> joiningGroups) {
        if (joiningGroups.isEmpty()) {
            return null;
        }

        final Long lastSelectedChallengeGroupId = lastSelectedChallengeGroupRecordRepository.findByMember(member)
            .map(LastSelectedChallengeGroupRecord::getChallengeGroupId)
            .orElseGet(() -> {
                final ChallengeGroup challengeGroup = joiningGroups.get(0);
                lastSelectedChallengeGroupRecordRepository.save(new LastSelectedChallengeGroupRecord(member, challengeGroup));
                return challengeGroup.getId();
            });

        return IntStream.range(0, joiningGroups.size())
            .filter(i -> joiningGroups.get(i).getId().equals(lastSelectedChallengeGroupId))
            .findFirst()
            .orElse(0);
    }

    @Transactional
    public void saveLastSelectedChallengeGroupRecord(final Long memberId, final Long groupId) {
        final Member member = getMember(memberId);
        final ChallengeGroup challengeGroup = getJoiningChallengeGroup(groupId);

        lastSelectedChallengeGroupRecordRepository.findByMember(member)
            .ifPresentOrElse(
                lastSelectedChallengeGroupRecord -> lastSelectedChallengeGroupRecord.updateChallengeGroup(challengeGroup),
                () -> lastSelectedChallengeGroupRecordRepository.save(new LastSelectedChallengeGroupRecord(member, challengeGroup)));
    }

    @Transactional
    public void leaveChallengeGroup(final Long memberId, final Long groupId) {
        final Member member = getMember(memberId);
        final ChallengeGroup challengeGroup = getJoiningChallengeGroup(groupId);

        final ChallengeGroupMember challengeGroupMember = challengeGroupMemberRepository.findByMemberAndChallengeGroup(member, challengeGroup)
            .orElseThrow(() -> new MemberNotInChallengeGroupException(
                String.format("해당 그룹에 속해있지 않습니다. (memberId : %d, groupId : %d)", memberId, groupId)));

        challengeGroupMemberRepository.delete(challengeGroupMember);
        saveFirstIndexGroupRecordToLastSelected(member);
    }

    private void saveFirstIndexGroupRecordToLastSelected(final Member member) {
        final List<ChallengeGroupMember> challengeGroupMembers = challengeGroupMemberRepository.findNotFinishedGroupByMember(member);
        final List<ChallengeGroup> joiningGroups = challengeGroupMembers.stream()
            .map(ChallengeGroupMember::getChallengeGroup)
            .toList();

        if (joiningGroups.isEmpty()) {
            return;
        }

        final ChallengeGroup firstIndexChallengeGroup = joiningGroups.get(0);
        lastSelectedChallengeGroupRecordRepository.findByMember(member)
            .ifPresentOrElse(
                lastSelectedChallengeGroupRecord -> lastSelectedChallengeGroupRecord.updateChallengeGroup(firstIndexChallengeGroup),
                () -> lastSelectedChallengeGroupRecordRepository.save(new LastSelectedChallengeGroupRecord(member, firstIndexChallengeGroup))
            );
    }

    public IsParticipatingChallengeGroupResponse isParticipatingChallengeGroup(Long memberId) {
        final Member member = getMember(memberId);
        final List<ChallengeGroupMember> challengeGroupMembers = challengeGroupMemberRepository.findNotFinishedGroupByMember(member);

        if (challengeGroupMembers.isEmpty()) {
            return new IsParticipatingChallengeGroupResponse(false);
        }
        return new IsParticipatingChallengeGroupResponse(true);
    }

    public List<ChallengeGroupMemberOverviewDto> getChallengeGroupMemberOverview(final Long memberId, final Long groupId) {
        final Member viewer = getMember(memberId);
        final ChallengeGroup challengeGroup = getJoiningChallengeGroup(groupId);
        validateChallengeGroupNotFinished(challengeGroup);

        final List<ChallengeGroupMember> groupMembers = challengeGroupMemberRepository.findAllByChallengeGroup(challengeGroup);
        final List<ChallengeGroupMemberWithAchievementRateDto> challengeGroupMemberWithAchievementRates = calculateChallengeGroupMemberAchievementRateSortedDesc(groupMembers);

        return buildChallengeGroupOverview(viewer, challengeGroupMemberWithAchievementRates);
    }

    private ChallengeGroup getJoiningChallengeGroup(final Long challengeGroupId) {
        return challengeGroupRepository.findById(challengeGroupId)
            .orElseThrow(() -> new ChallengeGroupNotFoundException(String.format("존재하지 않는 챌린지 그룹 id입니다. (%d)", challengeGroupId)));
    }

    private void validateChallengeGroupNotFinished(final ChallengeGroup joiningGroup) {
        if (joiningGroup.isFinished()) {
            throw new FinishedChallengeGroupException(
                String.format("이미 종료된 그룹입니다. (groupId: %d)", joiningGroup.getId()));
        }
    }

    private List<ChallengeGroupMemberWithAchievementRateDto> calculateChallengeGroupMemberAchievementRateSortedDesc(final List<ChallengeGroupMember> groupMembers) {
        final List<ChallengeGroupMemberWithAchievementRateDto> challengeGroupMemberWithAchievementRates = calculateChallengeGroupMembersAchievementRate(groupMembers);
        return sortByAchievementRateDesc(challengeGroupMemberWithAchievementRates);
    }

    private List<ChallengeGroupMemberWithAchievementRateDto> calculateChallengeGroupMembersAchievementRate(final List<ChallengeGroupMember> groupMembers) {
        return groupMembers.stream()
            .map(groupMember -> {
                final ChallengeGroup challengeGroup = groupMember.getChallengeGroup();
                final Member member = groupMember.getMember();
                final DailyTodoCertificationCount dailyTodoCertificationCount = dailyTodoCertificationRepository.countDailyTodoCertification(challengeGroup, member);
                final List<DailyTodo> todos = dailyTodoService.getMemberTodos(challengeGroup, groupMember.getMember());
                final int achievementRate = AchievementRateCalculator.calculate(
                    todos,
                    challengeGroup.getStartAt(),
                    challengeGroup.getEndAt(),
                    dailyTodoCertificationCount
                );

                return new ChallengeGroupMemberWithAchievementRateDto(groupMember, achievementRate);
            })
            .toList();
    }

    private List<ChallengeGroupMemberWithAchievementRateDto> sortByAchievementRateDesc(final List<ChallengeGroupMemberWithAchievementRateDto> challengeGroupMemberWithAchievementRates) {
        return challengeGroupMemberWithAchievementRates.stream()
            .sorted(Comparator.comparingInt(ChallengeGroupMemberWithAchievementRateDto::achievementRate).reversed())
            .toList();
    }

    private List<ChallengeGroupMemberOverviewDto> buildChallengeGroupOverview(final Member viewer, final List<ChallengeGroupMemberWithAchievementRateDto> challengeGroupMemberWithAchievementRates) {
        return challengeGroupMemberWithAchievementRates.stream()
            .map(dto -> new ChallengeGroupMemberOverviewDto(
                dto.challengeGroupMember().getMember().getId(),
                challengeGroupMemberWithAchievementRates.indexOf(dto) + 1,
                dto.challengeGroupMember().getMember().getProfileImageUrl(),
                dto.challengeGroupMember().getMember().getName(),
                dailyTodoHistoryService.getTodayDailyTodoHistoryReadStatus(
                    viewer,
                    dto.challengeGroupMember().getChallengeGroup(),
                    dto.challengeGroupMember().getMember()
                ),
                dto.achievementRate()
            ))
            .toList();
    }

    public int getMyRank(final Member target, final List<ChallengeGroupMember> groupMembers) {
        final List<ChallengeGroupMemberWithAchievementRateDto> challengeGroupMemberWithAchievementRate = calculateChallengeGroupMemberAchievementRateSortedDesc(groupMembers);
        final ChallengeGroupMemberWithAchievementRateDto myAchievementRate = challengeGroupMemberWithAchievementRate.stream()
            .filter(data -> data.challengeGroupMember().getMember().equals(target))
            .findFirst()
            .orElseThrow(() -> new MemberNotInChallengeGroupException(String.format("ChallengeGroup에서 해당하는 멤버를 찾을 수 없습니다. member={}, challengeGroup={}", target, groupMembers.get(0).getChallengeGroup())));

        return challengeGroupMemberWithAchievementRate.indexOf(myAchievementRate) + 1;
    }

    @Transactional
    public void updateChallengeGroupStatus() {
        List<ChallengeGroup> notFinishedGroups = challengeGroupRepository.findByStatusNot(ChallengeGroupStatus.FINISHED);

        for (ChallengeGroup notFinishedGroup : notFinishedGroups) {
            notFinishedGroup.updateStatus();
            log.info("챌린지 그룹 상태 업데이트: groupId={}, status={}", notFinishedGroup.getId(), notFinishedGroup.getStatus());
        }

        challengeGroupRepository.saveAll(notFinishedGroups);
    }
}
