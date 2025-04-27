package site.dogether.challengegroup.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
import site.dogether.challengegroup.exception.JoinGroupMaxCountException;
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

        validateJoinGroupMaxCount(member);

        final LocalDate startAt = request.challengeGroupStartAtOption().calculateStartAt();
        final LocalDate endAt = request.challengeGroupDurationOption().calculateEndAt(startAt);
        final ChallengeGroup challengeGroup = ChallengeGroup.create(
            request.groupName(),
            request.maximumMemberCount(),
            startAt,
            endAt
        );

        final ChallengeGroup savedChallengeGroup = challengeGroupRepository.save(challengeGroup);
        final ChallengeGroupMember challengeGroupMember = new ChallengeGroupMember(savedChallengeGroup, member);
        challengeGroupMemberRepository.save(challengeGroupMember);

        return challengeGroup.getJoinCode();
    }

    private void validateJoinGroupMaxCount(final Member member) {
        final int joiningGroupCount = challengeGroupMemberRepository.countNotFinishedGroupByMemberId(member.getId());
        if (joiningGroupCount >= 5) {
            log.info("해당 멤버의 참여중인 그룹이 5개입니다. memberId : {}", member.getId());
            throw new JoinGroupMaxCountException("참여할 수 있는 그룹은 최대 5개입니다.");
        }
    }

    @Transactional
    public JoinChallengeGroupDto joinChallengeGroup(final String joinCode, final Long memberId) {
        final Member joinMember = memberService.getMember(memberId);

        final ChallengeGroup challengeGroup = challengeGroupRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new InvalidChallengeGroupException("존재하지 않는 그룹입니다."));
        isGroupFinished(challengeGroup);

        final int maximumMemberCount = challengeGroup.getMaximumMemberCount();
        final int currentMemberCount = challengeGroupMemberRepository.countByChallengeGroup(challengeGroup);
        if (currentMemberCount >= maximumMemberCount) {
            throw new InvalidChallengeGroupException("그룹 인원이 가득 찼습니다.");
        }

        final ChallengeGroupMember challengeGroupMember = new ChallengeGroupMember(challengeGroup, joinMember);

        if (challengeGroupMemberRepository.existsByChallengeGroupAndMember(challengeGroup, joinMember)) {
            throw new InvalidChallengeGroupException("이미 참여 중인 그룹입니다.");
        }
        challengeGroupMemberRepository.save(challengeGroupMember);

        final List<ChallengeGroupMember> groupMembers =
                challengeGroupMemberRepository.findAllByChallengeGroup(challengeGroup);
        for (final ChallengeGroupMember groupMemberJpaEntity : groupMembers) {
            final Long groupMemberId = groupMemberJpaEntity.getMember().getId();
            if (groupMemberId.equals(joinMember.getId())) {
                continue;
            }
            notificationService.sendNotification(
                groupMemberId,
                "새로운 멤버가 참여했습니다.",
                joinMember.getName() + "님이 " + challengeGroup.getName() + " 그룹에 새로 합류했습니다.",
                "JOIN"
            );
        }

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd");
        final String startAtFormatted = challengeGroup.getStartAt().format(formatter);
        final String endAtFormatted = challengeGroup.getEndAt().format(formatter);

        return new JoinChallengeGroupDto(
            challengeGroup.getName(),
            challengeGroup.getDurationDays(),
            challengeGroup.getMaximumMemberCount(),
            startAtFormatted,
            endAtFormatted
        );
    }

    public List<JoiningChallengeGroupDto> getJoiningChallengeGroups(final Long memberId) {
        final Member member = memberService.getMember(memberId);

        final ChallengeGroupMember challengeGroupMember = challengeGroupMemberRepository.findByMember(member)
                        .orElseThrow(() -> new InvalidChallengeGroupException("그룹에 속해있지 않은 유저입니다."));
        final ChallengeGroup challengeGroup = challengeGroupMember.getChallengeGroup();
        isGroupFinished(challengeGroup);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd"); // TODO : 도메인으로 이동
        LocalDate endAt = challengeGroup.getEndAt();
        String endAtFormatted = endAt.format(formatter);

        long remainingDays = LocalDateTime.now().until(endAt, ChronoUnit.DAYS);

        return List.of(
                new JoiningChallengeGroupDto(
                    challengeGroup.getId(),
                    challengeGroup.getName(),
                    1,
                    10,
                    challengeGroup.getJoinCode(),
                    endAtFormatted,
                    challengeGroup.getDurationDays()
                )
        );
    }

    private static void isGroupFinished(final ChallengeGroup joiningGroup) {
        final boolean isFinishedGroup = joiningGroup.isFinished();
        if (isFinishedGroup) {
            throw new InvalidChallengeGroupException(String.format("이미 종료된 그룹입니다. (%s)", joiningGroup));
        }
    }

    public JoiningChallengeGroupMyActivityDto getJoiningChallengeGroupMyActivitySummary(final Long memberId) {
        final Member member = memberService.getMember(memberId);

        final ChallengeGroupMember challengeGroupMember =
                challengeGroupMemberRepository.findByMember(member)
                        .orElseThrow(() -> new InvalidChallengeGroupException("그룹에 속해있지 않은 유저입니다."));
        final ChallengeGroup joiningGroup = challengeGroupMember.getChallengeGroup();
        isGroupFinished(joiningGroup);

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

        isGroupFinished(challengeGroup);

        final List<Member> groupMembers = challengeGroupMemberRepository.findAllByChallengeGroup(challengeGroup)
                .stream()
                .map(ChallengeGroupMember::getMember)
                .toList();

        final List<MyTodoSummary> myTodoSummaries = dailyTodoService.getMyTodoSummaries(groupMembers, challengeGroup);
        final GroupTodoSummary groupTodoSummary = new GroupTodoSummary(myTodoSummaries);

        return List.of();
    }

    @Transactional
    public void leaveChallengeGroup(final Long memberId) {
        final Member member = memberService.getMember(memberId);

        final ChallengeGroupMember challengeGroupMember =
                challengeGroupMemberRepository.findByMember(member)
                        .orElseThrow(() -> new InvalidChallengeGroupException("그룹에 속해있지 않은 유저입니다."));
        final ChallengeGroup challengeGroup = challengeGroupMember.getChallengeGroup();
        isGroupFinished(challengeGroup);

        challengeGroupMemberRepository.delete(challengeGroupMember);
    }
}
