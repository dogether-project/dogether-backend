package site.dogether.dailytodocertification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.domain.ChallengeGroup;
import site.dogether.challengegroup.domain.ChallengeGroupStatus;
import site.dogether.challengegroup.service.exception.NotRunningChallengeGroupException;
import site.dogether.dailytodo.domain.DailyTodo;
import site.dogether.dailytodo.domain.DailyTodoStatus;
import site.dogether.dailytodo.infrastructure.entity.DailyTodoJpaEntity;
import site.dogether.dailytodocertification.domain.DailyTodoCertification;
import site.dogether.dailytodocertification.infrastructure.entity.DailyTodoCertificationJpaEntity;
import site.dogether.dailytodocertification.infrastructure.entity.DailyTodoCertificationMediaUrlJpaEntity;
import site.dogether.dailytodocertification.infrastructure.repository.DailyTodoCertificationJpaRepository;
import site.dogether.dailytodocertification.infrastructure.repository.DailyTodoCertificationMediaUrlJpaRepository;
import site.dogether.dailytodocertification.service.dto.DailyTodoCertificationDto;
import site.dogether.dailytodocertification.service.exception.DailyTodoCertificationNotFoundException;
import site.dogether.dailytodocertification.service.exception.NotDailyTodoCertificationReviewerException;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;
import site.dogether.member.service.MemberService;
import site.dogether.notification.service.NotificationService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DailyTodoCertificationService {

    private final DailyTodoCertificationJpaRepository dailyTodoCertificationJpaRepository;
    private final DailyTodoCertificationMediaUrlJpaRepository dailyTodoCertificationMediaUrlJpaRepository;
    private final NotificationService notificationService;
    private final MemberService memberService;

    @Transactional
    public void reviewDailyTodoCertification(
        final String authenticationToken,
        final Long dailyTodoCertificationId,
        final String reviewResult,
        final String rejectReason
    ) {
        final DailyTodoCertificationJpaEntity dailyTodoCertificationJpaEntity = dailyTodoCertificationJpaRepository.findById(dailyTodoCertificationId)
            .orElseThrow(() -> new DailyTodoCertificationNotFoundException("해당 id의 데일리 투두 수행 인증 정보가 존재하지 않습니다.))"));
        final MemberJpaEntity reviewerJpaEntity = memberService.findMemberEntityByAuthenticationToken(authenticationToken);
        final DailyTodoCertification dailyTodoCertification = dailyTodoCertificationJpaEntity.toDomain();
        checkDailyTodoCertificationReviewer(dailyTodoCertification, reviewerJpaEntity.getId());
        checkChallengeGroupIsRunning(dailyTodoCertification.getChallengeGroup());

        final DailyTodo reviewedDailyTodo = dailyTodoCertification.review(reviewResult, rejectReason);
        final DailyTodoJpaEntity dailyTodoJpaEntity = dailyTodoCertificationJpaEntity.getDailyTodo();
        dailyTodoJpaEntity.changeReviewResult(reviewedDailyTodo);

        final String notificationTitle = String.format("%s님이 투두 수행 인증을 검사해줬어요! 🫣", reviewerJpaEntity.getName());
        final String notificationMessage = String.format("투두 내용 : %s\n검사 결과 : %s", reviewedDailyTodo.getContent(), reviewedDailyTodo.getStatusDescription());
        notificationService.sendNotification(reviewedDailyTodo.getMemberId(), notificationTitle, notificationMessage);
    }

    private void checkDailyTodoCertificationReviewer(final DailyTodoCertification dailyTodoCertification, final Long reviewer) {
        if (!dailyTodoCertification.checkReviewer(reviewer)) {
            throw new NotDailyTodoCertificationReviewerException("해당 데일리 투두 수행 인증의 검사자가 아닙니다.");
        }
    }

    private void checkChallengeGroupIsRunning(final ChallengeGroup challengeGroup) {
        if (!challengeGroup.isRunning()) {
            throw new NotRunningChallengeGroupException("현재 진행중인 챌린지 그룹이 아닙니다.");
        }
    }

    public List<DailyTodoCertificationDto> findAllTodoCertificationsForReview(final String authenticationToken) {
        final MemberJpaEntity reviewerJpaEntity = memberService.findMemberEntityByAuthenticationToken(authenticationToken);
        final List<DailyTodoCertificationJpaEntity> dailyTodoCertificationsForReview = dailyTodoCertificationJpaRepository.findAllByReviewerAndDailyTodo_StatusAndDailyTodo_ChallengeGroup_Status(
            reviewerJpaEntity,
            DailyTodoStatus.REVIEW_PENDING,
            ChallengeGroupStatus.RUNNING);

        return dailyTodoCertificationsForReview.stream()
            .map(dailyTodoCertificationJpaEntity -> DailyTodoCertificationDto.from(
                dailyTodoCertificationJpaEntity.toDomain(),
                findAllDailyTodoCertificationMediaUrlValuesByDailyTodoCertification(dailyTodoCertificationJpaEntity)))
            .toList();
    }

    private List<String> findAllDailyTodoCertificationMediaUrlValuesByDailyTodoCertification(final DailyTodoCertificationJpaEntity dailyTodoCertificationJpaEntity) {
        return dailyTodoCertificationMediaUrlJpaRepository.findAllByDailyTodoCertification(dailyTodoCertificationJpaEntity)
            .stream()
            .map(DailyTodoCertificationMediaUrlJpaEntity::getValue)
            .toList();
    }
}
