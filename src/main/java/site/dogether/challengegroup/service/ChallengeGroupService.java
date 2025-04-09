package site.dogether.challengegroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.controller.request.CreateChallengeGroupRequest;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;
import site.dogether.challengegroup.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupInfo;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupMyActivityDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupTeamActivityDto;
import site.dogether.dailytodo.entity.GroupTodoSummary;
import site.dogether.dailytodo.entity.MyTodoSummary;
import site.dogether.dailytodo.service.DailyTodoService;
import site.dogether.member.entity.Member;
import site.dogether.member.service.MemberService;
import site.dogether.notification.service.NotificationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
        memberAlreadyInGroup(member);

        final LocalDateTime startAt = request.challengeGroupStartAtOption().calculateStartAt();
        final LocalDateTime endAt = request.challengeGroupDurationOption().calculateEndAt(startAt);
        final ChallengeGroup challengeGroup = ChallengeGroup.create(
            request.name(),
            request.maximumMemberCount(),
            startAt,
            endAt,
            request.maximumTodoCount()
        );

        final ChallengeGroup savedChallengeGroup = challengeGroupRepository.save(challengeGroup);
        final ChallengeGroupMember challengeGroupMember = new ChallengeGroupMember(savedChallengeGroup, member);
        challengeGroupMemberRepository.save(challengeGroupMember);

        return challengeGroup.getJoinCode();
    }

    @Transactional
    public JoinChallengeGroupDto joinChallengeGroup(final String joinCode, final Long memberId) {
        final Member joinMember = memberService.getMember(memberId);
        memberAlreadyInGroup(joinMember);

        final ChallengeGroup challengeGroup = challengeGroupRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new InvalidChallengeGroupException("존재하지 않는 그룹입니다."));
        isGroupFinished(challengeGroup);

        final int maximumMemberCount = challengeGroup.getMaximumMemberCount();
        final int currentMemberCount = challengeGroupMemberRepository.countByChallengeGroup(challengeGroup);
        if (currentMemberCount >= maximumMemberCount) {
            throw new InvalidChallengeGroupException("그룹 인원이 가득 찼습니다.");
        }

        final ChallengeGroupMember challengeGroupMember = new ChallengeGroupMember(challengeGroup, joinMember);
        challengeGroupMemberRepository.save(challengeGroupMember);

        // TODO : 질문! 이거 영재님이 주석하셨나요?
//        notificationService.sendNotification(
//                joinMember.getId(),
//                "챌린지 그룹에 참여하였습니다.",
//                "그룹명 : " + challengeGroup.getName()
//        );

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

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        final String startAtFormatted = challengeGroup.getStartAt().format(formatter);
        final String endAtFormatted = challengeGroup.getEndAt().format(formatter);

        return new JoinChallengeGroupDto(
            challengeGroup.getName(),
            challengeGroup.getMaximumMemberCount(),
            startAtFormatted,
            endAtFormatted,
            challengeGroup.getDurationDays()
        );
    }

    public JoiningChallengeGroupInfo getJoiningChallengeGroupInfo(final Long memberId) {
        final Member member = memberService.getMember(memberId);

        final ChallengeGroupMember challengeGroupMember =
                challengeGroupMemberRepository.findByMember(member)
                        .orElseThrow(() -> new InvalidChallengeGroupException("그룹에 속해있지 않은 유저입니다."));
        final ChallengeGroup challengeGroup = challengeGroupMember.getChallengeGroup();
        isGroupFinished(challengeGroup);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd"); // TODO : 도메인으로 이동
        LocalDateTime endAt = challengeGroup.getEndAt();
        String endAtFormatted = endAt.format(formatter);

        long remainingDays = LocalDateTime.now().until(endAt, ChronoUnit.DAYS);

        return new JoiningChallengeGroupInfo(
            challengeGroup.getName(),
            challengeGroup.getDurationDays(),
            challengeGroup.getJoinCode(),
            challengeGroup.getMaximumTodoCount(),
            endAtFormatted,
            (int) remainingDays
        );
    }

    private static void isGroupFinished(final ChallengeGroup joiningGroup) {
        final boolean isFinishedGroup = joiningGroup.isFinished();
        if (isFinishedGroup) {
            throw new InvalidChallengeGroupException(String.format("이미 종료된 그룹입니다. (%s)", joiningGroup));
        }
    }

    private void memberAlreadyInGroup(final Member groupCreator) {
        final boolean isAlreadyInGroup = challengeGroupMemberRepository.existsByMember(groupCreator);
        if (isAlreadyInGroup) {
            throw new InvalidChallengeGroupException("이미 그룹에 속해있는 유저입니다.");
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

    public JoiningChallengeGroupTeamActivityDto getJoiningChallengeGroupTeamActivitySummary(final Long memberId) {
        final Member member = memberService.getMember(memberId);

        final ChallengeGroupMember challengeGroupMember =
                challengeGroupMemberRepository.findByMember(member)
                        .orElseThrow(() -> new InvalidChallengeGroupException("그룹에 속해있지 않은 유저입니다."));
        final ChallengeGroup joiningGroup = challengeGroupMember.getChallengeGroup();
        isGroupFinished(joiningGroup);

        final List<Member> groupMembers = challengeGroupMemberRepository.findAllByChallengeGroup(joiningGroup)
                .stream()
                .map(ChallengeGroupMember::getMember)
                .toList();

        final List<MyTodoSummary> myTodoSummaries = dailyTodoService.getMyTodoSummaries(groupMembers, joiningGroup);
        final GroupTodoSummary groupTodoSummary = new GroupTodoSummary(myTodoSummaries);

        return new JoiningChallengeGroupTeamActivityDto(
                groupTodoSummary.getRanks()
        );
    }

    public boolean isJoiningChallengeGroup(final Long memberId) {
        final Member member = memberService.getMember(memberId);
        final ChallengeGroupMember challengeGroupMember =
                challengeGroupMemberRepository.findByMember(member).orElse(null);
        if (challengeGroupMember == null) {
            return false;
        }
        final ChallengeGroup joiningGroup = challengeGroupMember.getChallengeGroup();

        return !joiningGroup.isFinished();
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

    public ChallengeGroupStatus getMyChallengeGroupStatus(final Long memberId) {
        final Member member = memberService.getMember(memberId);
        final ChallengeGroupMember challengeGroupMember = challengeGroupMemberRepository.findByMember(member)
            .orElseThrow(() -> new MemberNotInChallengeGroupException("회원이 속한 챌린지 그룹이 존재하지 않습니다."));
        return challengeGroupMember.getChallengeGroup().getStatus();
    }
}
