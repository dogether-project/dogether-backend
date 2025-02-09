package site.dogether.dailytodo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.domain.ChallengeGroup;
import site.dogether.challengegroup.domain.ChallengeGroupStatus;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupJpaEntity;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupMemberJpaEntity;
import site.dogether.challengegroup.infrastructure.repository.ChallengeGroupMemberJpaRepository;
import site.dogether.challengegroup.service.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.service.exception.NotEnoughChallengeGroupMembersException;
import site.dogether.challengegroup.service.exception.NotRunningChallengeGroupException;
import site.dogether.dailytodo.domain.DailyTodo;
import site.dogether.dailytodo.domain.DailyTodoStatus;
import site.dogether.dailytodo.infrastructure.entity.DailyTodoJpaEntity;
import site.dogether.dailytodo.infrastructure.repository.DailyTodoJpaRepository;
import site.dogether.dailytodo.service.exception.*;
import site.dogether.dailytodocertification.domain.DailyTodoCertification;
import site.dogether.dailytodocertification.domain.DailyTodoCertificationMediaUrls;
import site.dogether.dailytodocertification.infrastructure.entity.DailyTodoCertificationJpaEntity;
import site.dogether.dailytodocertification.infrastructure.entity.DailyTodoCertificationMediaUrlJpaEntity;
import site.dogether.dailytodocertification.infrastructure.repository.DailyTodoCertificationJpaRepository;
import site.dogether.dailytodocertification.infrastructure.repository.DailyTodoCertificationMediaUrlJpaRepository;
import site.dogether.member.domain.Member;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;
import site.dogether.member.service.MemberService;
import site.dogether.notification.service.NotificationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DailyTodoService {

    private static final int MINIMUM_NEED_CHALLENGE_GROUP_MEMBER_COUNT = 2;
    private static final Random random = new Random(); // TODO : 테스트를 위해 추상화

    private final ChallengeGroupMemberJpaRepository challengeGroupMemberJpaRepository;
    private final DailyTodoJpaRepository dailyTodoJpaRepository;
    private final DailyTodoCertificationJpaRepository dailyTodoCertificationJpaRepository;
    private final DailyTodoCertificationMediaUrlJpaRepository dailyTodoCertificationMediaUrlJpaRepository;
    private final NotificationService notificationService;
    private final MemberService memberService;

    @Transactional
    public void saveDailyTodo(final String authenticationToken, final List<String> dailyTodoContents) {
        final MemberJpaEntity memberJpaEntity = memberService.findMemberEntityByAuthenticationToken(authenticationToken);
        final ChallengeGroupMemberJpaEntity challengeGroupMemberJpaEntity = challengeGroupMemberJpaRepository.findByChallengeGroup_StatusAndMember(ChallengeGroupStatus.RUNNING, memberJpaEntity)
            .orElseThrow(() -> new MemberNotInChallengeGroupException("현재 진행중인 챌린지 그룹에 참여하고 있지 않습니다."));
        checkExistPendingReviewDailyTodos(memberJpaEntity);
        checkGroupMemberCounts(challengeGroupMemberJpaEntity);

        final List<DailyTodoJpaEntity> dailyTodoJpaEntities = convertDailyTodoContentsToEntities(dailyTodoContents, challengeGroupMemberJpaEntity, memberJpaEntity);
        dailyTodoJpaRepository.saveAll(dailyTodoJpaEntities);
    }

    private void checkExistPendingReviewDailyTodos(final MemberJpaEntity memberJpaEntity) {
        final boolean existPendingReviewDailyTodos = dailyTodoCertificationJpaRepository.existsByDailyTodo_StatusAndReviewer(DailyTodoStatus.REVIEW_PENDING, memberJpaEntity);
        if (existPendingReviewDailyTodos) {
            throw new UnreviewedDailyTodoExistsException("아직 검사하지 않은 데일리 투두가 존재합니다.");
        }
    }

    private void checkGroupMemberCounts(final ChallengeGroupMemberJpaEntity challengeGroupMemberJpaEntity) {
        final List<ChallengeGroupMemberJpaEntity> challengeGroupMembers = challengeGroupMemberJpaRepository.findAllByChallengeGroup(challengeGroupMemberJpaEntity.getChallengeGroup());
        if (challengeGroupMembers.size() < MINIMUM_NEED_CHALLENGE_GROUP_MEMBER_COUNT) {
            throw new NotEnoughChallengeGroupMembersException("챌린지 진행에 필요한 그룹 인원수가 부족합니다.");
        }
    }

    private List<DailyTodoJpaEntity> convertDailyTodoContentsToEntities(
        final List<String> dailyTodoContents,
        final ChallengeGroupMemberJpaEntity challengeGroupMemberJpaEntity,
        final MemberJpaEntity memberJpaEntity
    ) {
        final ChallengeGroup challengeGroup = challengeGroupMemberJpaEntity.toChallengeGroupDomain();
        challengeGroup.checkEnableTodoCount(dailyTodoContents.size());

        return dailyTodoContents.stream()
            .map(dailyTodoContent -> DailyTodo.create(dailyTodoContent, challengeGroupMemberJpaEntity.toMemberDomain(), challengeGroup))
            .map(dailyTodo -> new DailyTodoJpaEntity(dailyTodo, challengeGroupMemberJpaEntity.getChallengeGroup(), memberJpaEntity))
            .toList();
    }

    @Transactional
    public void certifyDailyTodo(
        final String authenticationToken,
        final Long dailyTodoId,
        final String content,
        final List<String> mediaUrls
    ) {
        final DailyTodoJpaEntity dailyTodoJpaEntity = dailyTodoJpaRepository.findById(dailyTodoId)
            .orElseThrow(() -> new DailyTodoNotFoundException("해당 id의 데일리 투두가 존재하지 않습니다."));

        final MemberJpaEntity certifyingMember = memberService.findMemberEntityByAuthenticationToken(authenticationToken);
        final DailyTodo dailyTodo = dailyTodoJpaEntity.toDomain();
        checkDailyTodoOwner(dailyTodo, certifyingMember.getId());

        final ChallengeGroupJpaEntity challengeGroupJpaEntity = dailyTodoJpaEntity.getChallengeGroup();
        checkChallengeGroupIsRunning(challengeGroupJpaEntity.toDomain());
        checkDailyTodoStatusIsCertifyPending(dailyTodo);
        checkDailyTodoCreatedToday(dailyTodo);

        final Member member = certifyingMember.toDomain();
        final DailyTodoCertification dailyTodoCertification = DailyTodoCertification.create(content, dailyTodo, member);

        final DailyTodoCertificationMediaUrls dailyTodoCertificationMediaUrls = new DailyTodoCertificationMediaUrls(mediaUrls);

        final MemberJpaEntity dailyTodoCertificationReviewer = pickDailyTodoCertificationReviewer(challengeGroupJpaEntity, certifyingMember);

        final DailyTodoCertificationJpaEntity dailyTodoCertificationJpaEntity = new DailyTodoCertificationJpaEntity(dailyTodoCertification, dailyTodoJpaEntity, dailyTodoCertificationReviewer);
        dailyTodoCertificationJpaRepository.save(dailyTodoCertificationJpaEntity);

        final List<DailyTodoCertificationMediaUrlJpaEntity> dailyTodoCertificationMediaUrlJpaEntities = dailyTodoCertificationMediaUrls.getValues().stream()
            .map(mediaUrlValue -> new DailyTodoCertificationMediaUrlJpaEntity(mediaUrlValue, dailyTodoCertificationJpaEntity))
            .toList();
        dailyTodoCertificationMediaUrlJpaRepository.saveAll(dailyTodoCertificationMediaUrlJpaEntities);
        dailyTodoJpaEntity.changeStatusReviewPending();

        notificationService.sendNotification(
            dailyTodoCertificationReviewer.getId(),
            certifyingMember.getName() + "님의 투두 수행 검사자로 배정되었습니다!",
            "투두 내용 : " + dailyTodo.getContent()
        );
        notificationService.sendNotification(
            certifyingMember.getId(),
            dailyTodoCertificationReviewer.getName() + "님이 투두 수행 검사자로 배정되었습니다!",
            "투두 내용 : " + dailyTodo.getContent()
        );
    }

    private void checkDailyTodoOwner(final DailyTodo dailyTodo, final Long memberId) {
        if (!dailyTodo.checkOwner(memberId)) {
            throw new NotDailyTodoOwnerException("해당 데일리 투두의 작성자가 아닙니다.");
        }
    }

    private void checkChallengeGroupIsRunning(final ChallengeGroup challengeGroup) {
        if (!challengeGroup.isRunning()) {
            throw new NotRunningChallengeGroupException("현재 진행중인 챌린지 그룹이 아닙니다.");
        }
    }

    private void checkDailyTodoStatusIsCertifyPending(final DailyTodo dailyTodo) {
        if (!dailyTodo.isCertifyPendingStatus()) {
            throw new DailyTodoStatusException("데일리 투두가 인증 대기 상태가 아닙니다.");
        }
    }

    private void checkDailyTodoCreatedToday(final DailyTodo dailyTodo) {
        if (!dailyTodo.createdToday()) {
            throw new DailyTodoCreatedDateException("오늘 작성한 데일리 투두만 인증할 수 있습니다.");
        }
    }

    private MemberJpaEntity pickDailyTodoCertificationReviewer(final ChallengeGroupJpaEntity challengeGroup, final MemberJpaEntity certifyingMember) {
        final List<MemberJpaEntity> otherChallengeGroupMembers = new ArrayList<>(
            challengeGroupMemberJpaRepository.findAllByChallengeGroup(challengeGroup)
                .stream()
                .map(ChallengeGroupMemberJpaEntity::getMember)
                .filter(member -> !member.getId().equals(certifyingMember.getId())) // targetMemberId를 가진 멤버 제외
                .toList()
        );

        if (otherChallengeGroupMembers.isEmpty()) {
            log.error("투두 수행 인증 시점에 챌린지 그룹에 사용자가 존재하지 않음. - {}", challengeGroup);
            throw new NotEnoughChallengeGroupMembersException("챌린지 그룹에 사용자가 존재하지 않습니다.");
        }

        final MemberJpaEntity pickedReviewer = otherChallengeGroupMembers.get(random.nextInt(otherChallengeGroupMembers.size()));
        log.info("데일리 투두 수행 인증 검사자 배정 완료 : 투두 수행 인증자 - {}, 투두 수행 검사자 - {}", certifyingMember, pickedReviewer);
        return pickedReviewer;
    }

    public List<String> findYesterdayDailyTodos(final String authenticationToken) {
        final MemberJpaEntity memberJpaEntity = memberService.findMemberEntityByAuthenticationToken(authenticationToken);
        final ChallengeGroupMemberJpaEntity challengeGroupMemberJpaEntity = challengeGroupMemberJpaRepository.findByChallengeGroup_StatusAndMember(ChallengeGroupStatus.RUNNING, memberJpaEntity)
            .orElseThrow(() -> new MemberNotInChallengeGroupException("현재 진행중인 챌린지 그룹에 참여하고 있지 않습니다."));

        final LocalDate yesterday = LocalDate.now().minusDays(1);
        return dailyTodoJpaRepository.findAllByCreatedAtBetweenAndChallengeGroupAndMember(
            yesterday.atStartOfDay(),
            yesterday.atTime(LocalTime.MAX),
            challengeGroupMemberJpaEntity.getChallengeGroup(),
            challengeGroupMemberJpaEntity.getMember())
            .stream()
            .map(DailyTodoJpaEntity::getContent)
            .toList();
    }
}
