package site.dogether.dailytodo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.domain.ChallengeGroup;
import site.dogether.challengegroup.domain.ChallengeGroupStatus;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupMemberJpaEntity;
import site.dogether.challengegroup.infrastructure.repository.ChallengeGroupMemberJpaRepository;
import site.dogether.dailytodo.domain.DailyTodo;
import site.dogether.dailytodo.domain.DailyTodoStatus;
import site.dogether.dailytodo.infrastructure.entity.DailyTodoJpaEntity;
import site.dogether.dailytodo.infrastructure.repository.DailyTodoJpaRepository;
import site.dogether.challengegroup.service.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.service.exception.NotEnoughChallengeGroupMembersException;
import site.dogether.dailytodo.service.exception.UnreviewedDailyTodoExistsException;
import site.dogether.dailytodocertification.infrastructure.repository.DailyTodoCertificationJpaRepository;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;
import site.dogether.member.service.MemberService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DailyTodoService {

    private static final int MINIMUM_NEED_CHALLENGE_GROUP_MEMBER_COUNT = 2;

    private final ChallengeGroupMemberJpaRepository challengeGroupMemberJpaRepository;
    private final DailyTodoCertificationJpaRepository dailyTodoCertificationJpaRepository;
    private final DailyTodoJpaRepository dailyTodoJpaRepository;
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
}
