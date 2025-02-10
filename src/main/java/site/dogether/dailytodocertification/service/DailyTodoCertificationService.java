package site.dogether.dailytodocertification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.domain.ChallengeGroup;
import site.dogether.challengegroup.service.exception.NotRunningChallengeGroupException;
import site.dogether.dailytodo.domain.DailyTodo;
import site.dogether.dailytodo.infrastructure.entity.DailyTodoJpaEntity;
import site.dogether.dailytodocertification.domain.DailyTodoCertification;
import site.dogether.dailytodocertification.infrastructure.entity.DailyTodoCertificationJpaEntity;
import site.dogether.dailytodocertification.infrastructure.repository.DailyTodoCertificationJpaRepository;
import site.dogether.dailytodocertification.service.exception.DailyTodoCertificationNotFoundException;
import site.dogether.dailytodocertification.service.exception.NotDailyTodoCertificationReviewerException;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;
import site.dogether.member.service.MemberService;
import site.dogether.notification.service.NotificationService;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DailyTodoCertificationService {

    private final DailyTodoCertificationJpaRepository dailyTodoCertificationJpaRepository;
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
}
