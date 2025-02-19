package site.dogether.challengegroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.controller.request.CreateChallengeGroupRequest;
import site.dogether.challengegroup.domain.ChallengeGroup;
import site.dogether.challengegroup.domain.ChallengeGroupStatus;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupJpaEntity;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupMemberJpaEntity;
import site.dogether.challengegroup.infrastructure.repository.ChallengeGroupJpaRepository;
import site.dogether.challengegroup.infrastructure.repository.ChallengeGroupMemberJpaRepository;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupInfo;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupMyActivityDto;
import site.dogether.challengegroup.service.exception.MemberNotInChallengeGroupException;
import site.dogether.dailytodo.domain.GroupTodoSummary;
import site.dogether.dailytodo.domain.MyTodoSummary;
import site.dogether.dailytodo.service.DailyTodoService;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;
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

    private final ChallengeGroupJpaRepository challengeGroupJpaRepository;
    private final ChallengeGroupMemberJpaRepository challengeGroupMemberJpaRepository;
    private final NotificationService notificationService;
    private final MemberService memberService;
    private final DailyTodoService dailyTodoService;

    @Transactional
    public String createChallengeGroup(final CreateChallengeGroupRequest request, final String authenticationToken) {
        final MemberJpaEntity groupCreatorJpaEntity = memberService.findMemberEntityByAuthenticationToken(authenticationToken);
        memberAlreadyInGroup(groupCreatorJpaEntity);

        ChallengeGroup challengeGroup = new ChallengeGroup(
                request.name(),
                request.maximumMemberCount(),
                request.startAt(),
                request.durationOption(),
                request.maximumTodoCount()
        );

        final ChallengeGroupJpaEntity challengeGroupJpaEntity = ChallengeGroupJpaEntity.from(challengeGroup);
        challengeGroup = challengeGroupJpaRepository.save(challengeGroupJpaEntity).toDomain();

        final ChallengeGroupMemberJpaEntity challengeGroupMemberJpaEntity = new ChallengeGroupMemberJpaEntity(
                challengeGroupJpaEntity, groupCreatorJpaEntity
        );
        challengeGroupMemberJpaRepository.save(challengeGroupMemberJpaEntity);

        return challengeGroup.getJoinCode();
    }

    @Transactional
    public void joinChallengeGroup(final String joinCode, final String authenticationToken) {
        final MemberJpaEntity joinMember = memberService.findMemberEntityByAuthenticationToken(authenticationToken);
        memberAlreadyInGroup(joinMember);

        final ChallengeGroupJpaEntity challengeGroupJpaEntity = challengeGroupJpaRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new InvalidChallengeGroupException("존재하지 않는 그룹입니다."));
        final ChallengeGroup joinGroup = challengeGroupJpaEntity.toDomain();

        isGroupFinished(joinGroup);

        final int maximumMemberCount = joinGroup.getMaximumMemberCount();
        final int currentMemberCount = challengeGroupMemberJpaRepository.countByChallengeGroup(challengeGroupJpaEntity);
        if (currentMemberCount >= maximumMemberCount) {
            throw new InvalidChallengeGroupException("그룹 인원이 가득 찼습니다.");
        }

        final ChallengeGroupMemberJpaEntity challengeGroupMemberJpaEntity = new ChallengeGroupMemberJpaEntity(
                challengeGroupJpaEntity, joinMember
        );
        challengeGroupMemberJpaRepository.save(challengeGroupMemberJpaEntity);

        notificationService.sendNotification(
                joinMember.getId(),
                "챌린지 그룹에 참여하였습니다.",
                "그룹명 : " + joinGroup.getName()
        );

        final List<ChallengeGroupMemberJpaEntity> groupMembers =
                challengeGroupMemberJpaRepository.findAllByChallengeGroup(challengeGroupJpaEntity);
        for (final ChallengeGroupMemberJpaEntity groupMemberJpaEntity : groupMembers) {
            final Long groupMemberId = groupMemberJpaEntity.getMember().getId();
            if (groupMemberId.equals(joinMember.getId())) {
                continue;
            }
            notificationService.sendNotification(
                    groupMemberId,
                    "새로운 멤버가 참여했습니다.",
                    joinMember.getName() + "님이 " + joinGroup.getName() + " 그룹에 새로 합류했습니다."
            );
        }
    }

    public JoiningChallengeGroupInfo getJoiningChallengeGroupInfo(final String authenticationToken) {
        final MemberJpaEntity memberJpaEntity = memberService.findMemberEntityByAuthenticationToken(authenticationToken);

        final ChallengeGroupMemberJpaEntity challengeGroupMemberJpaEntity =
                challengeGroupMemberJpaRepository.findByMember(memberJpaEntity)
                        .orElseThrow(() -> new InvalidChallengeGroupException("그룹에 속해있지 않은 유저입니다."));
        final ChallengeGroupJpaEntity challengeGroupJpaEntity = challengeGroupMemberJpaEntity.getChallengeGroup();
        final ChallengeGroup joiningGroup = challengeGroupJpaEntity.toDomain();
        isGroupFinished(joiningGroup);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // TODO : 도메인으로 이동
        LocalDateTime endAt = challengeGroupJpaEntity.getEndAt();
        String endAtFormatted = endAt.format(formatter);

        long remainingDays = LocalDateTime.now().until(endAt, ChronoUnit.DAYS);

        return new JoiningChallengeGroupInfo(
                joiningGroup.getName(),
                joiningGroup.getDurationOption().getValue(),
                joiningGroup.getJoinCode(),
                endAtFormatted,
                (int) remainingDays
        );
    }

    private static void isGroupFinished(final ChallengeGroup joiningGroup) {
        final boolean isFinishedGroup = joiningGroup.isFinished();
        if (isFinishedGroup) {
            throw new InvalidChallengeGroupException("이미 종료된 그룹입니다.");
        }
    }

    private void memberAlreadyInGroup(final MemberJpaEntity groupCreatorJpaEntity) {
        final boolean isAlreadyInGroup = challengeGroupMemberJpaRepository.existsByMember(groupCreatorJpaEntity);
        if (isAlreadyInGroup) {
            throw new InvalidChallengeGroupException("이미 그룹에 속해있는 유저입니다.");
        }
    }

    public JoiningChallengeGroupMyActivityDto getJoiningChallengeGroupMyActivitySummary(final String authenticationToken) {
        final MemberJpaEntity memberJpaEntity = memberService.findMemberEntityByAuthenticationToken(authenticationToken);

        final ChallengeGroupMemberJpaEntity challengeGroupMemberJpaEntity =
                challengeGroupMemberJpaRepository.findByMember(memberJpaEntity)
                        .orElseThrow(() -> new InvalidChallengeGroupException("그룹에 속해있지 않은 유저입니다."));
        final ChallengeGroupJpaEntity joiningGroupEntity = challengeGroupMemberJpaEntity.getChallengeGroup();
        final ChallengeGroup joiningGroup = joiningGroupEntity.toDomain();
        isGroupFinished(joiningGroup);

        final MyTodoSummary myTodoSummary = dailyTodoService.getMyTodoSummary(memberJpaEntity, joiningGroupEntity);

        return new JoiningChallengeGroupMyActivityDto(
                myTodoSummary.calculateTotalTodoCount(),
                myTodoSummary.calculateTotalCertificatedCount(),
                myTodoSummary.calculateTotalApprovedCount(),
                myTodoSummary.calculateTotalRejectedCount()
        );
    }

    public JoiningChallengeGroupTeamActivityDto getJoiningChallengeGroupTeamActivitySummary(final String authenticationToken) {
        final MemberJpaEntity memberJpaEntity = memberService.findMemberEntityByAuthenticationToken(authenticationToken);

        final ChallengeGroupMemberJpaEntity challengeGroupMemberJpaEntity =
                challengeGroupMemberJpaRepository.findByMember(memberJpaEntity)
                        .orElseThrow(() -> new InvalidChallengeGroupException("그룹에 속해있지 않은 유저입니다."));
        final ChallengeGroupJpaEntity joiningGroupEntity = challengeGroupMemberJpaEntity.getChallengeGroup();
        final ChallengeGroup joiningGroup = joiningGroupEntity.toDomain();
        isGroupFinished(joiningGroup);

        final List<MemberJpaEntity> groupMembers = challengeGroupMemberJpaRepository.findAllByChallengeGroup(joiningGroupEntity)
                .stream()
                .map(ChallengeGroupMemberJpaEntity::getMember)
                .toList();

        final List<MyTodoSummary> myTodoSummaries = dailyTodoService.getMyTodoSummaries(groupMembers, joiningGroupEntity);
        final GroupTodoSummary groupTodoSummary = new GroupTodoSummary(myTodoSummaries);

        return new JoiningChallengeGroupTeamActivityDto(
                groupTodoSummary.getRanks()
        );
    }

    public boolean isJoiningChallengeGroup(final String authenticationToken) {
        final MemberJpaEntity memberJpaEntity = memberService.findMemberEntityByAuthenticationToken(authenticationToken);
        final ChallengeGroupMemberJpaEntity challengeGroupMemberJpaEntity =
                challengeGroupMemberJpaRepository.findByMember(memberJpaEntity).orElse(null);
        if (challengeGroupMemberJpaEntity == null) {
            return false;
        }
        final ChallengeGroupJpaEntity joiningGroupEntity = challengeGroupMemberJpaEntity.getChallengeGroup();
        final ChallengeGroup joiningGroup = joiningGroupEntity.toDomain();

        return joiningGroup.isRunning();
    }

    @Transactional
    public void leaveChallengeGroup(final String authenticationToken) {
        final MemberJpaEntity memberJpaEntity = memberService.findMemberEntityByAuthenticationToken(authenticationToken);

        final ChallengeGroupMemberJpaEntity challengeGroupMemberJpaEntity =
                challengeGroupMemberJpaRepository.findByMember(memberJpaEntity)
                        .orElseThrow(() -> new InvalidChallengeGroupException("그룹에 속해있지 않은 유저입니다."));
        final ChallengeGroupJpaEntity challengeGroupJpaEntity = challengeGroupMemberJpaEntity.getChallengeGroup();
        final ChallengeGroup challengeGroup = challengeGroupJpaEntity.toDomain();
        isGroupFinished(challengeGroup);

        challengeGroupMemberJpaRepository.delete(challengeGroupMemberJpaEntity);
    }

    public ChallengeGroupStatus getMyChallengeGroupStatus(final String authenticationToken) {
        final MemberJpaEntity memberJpaEntity = memberService.findMemberEntityByAuthenticationToken(authenticationToken);
        final ChallengeGroupMemberJpaEntity challengeGroupMemberJpaEntity = challengeGroupMemberJpaRepository.findByMember(memberJpaEntity)
            .orElseThrow(() -> new MemberNotInChallengeGroupException("회원이 속한 챌린지 그룹이 존재하지 않습니다."));
        return challengeGroupMemberJpaEntity.getChallengeGroup().getStatus();
    }
}
