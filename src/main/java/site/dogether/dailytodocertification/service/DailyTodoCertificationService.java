package site.dogether.dailytodocertification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.exception.DailyTodoCertificationNotFoundException;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationRepository;
import site.dogether.dailytodocertification.service.dto.DailyTodoCertificationDto;
import site.dogether.dailytodohistory.service.DailyTodoHistoryService;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;
import site.dogether.notification.service.NotificationService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DailyTodoCertificationService {

    private final MemberRepository memberRepository;
    private final DailyTodoCertificationRepository dailyTodoCertificationRepository;
    private final DailyTodoHistoryService dailyTodoHistoryService;
    private final NotificationService notificationService;

    @Transactional
    public void reviewDailyTodoCertification(
        final Long reviewerId,
        final Long dailyTodoCertificationId,
        final String reviewResult,
        final String rejectReason
    ) {
        final Member reviewer = getMember(reviewerId);
        final DailyTodoCertification dailyTodoCertification = getDailyTodoCertification(dailyTodoCertificationId);
        final DailyTodo dailyTodo = dailyTodoCertification.getDailyTodo();

        dailyTodo.review(reviewer, dailyTodoCertification, DailyTodoStatus.convertFromValue(reviewResult), rejectReason);

        dailyTodoHistoryService.saveDailyTodoHistory(dailyTodo, dailyTodoCertification);
        sendReviewResultNotificationToDailyTodoWriter(dailyTodo);
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 id입니다. (%d)", memberId)));
    }

    private DailyTodoCertification getDailyTodoCertification(final Long dailyTodoCertificationId) {
        return dailyTodoCertificationRepository.findById(dailyTodoCertificationId)
            .orElseThrow(() -> new DailyTodoCertificationNotFoundException(String.format("존재하지 않는 데일리 투두 인증 id입니다. (%d)", dailyTodoCertificationId)));
    }

    private void sendReviewResultNotificationToDailyTodoWriter(final DailyTodo dailyTodo) {
        notificationService.sendNotification(
            dailyTodo.getWriterId(),
            "투두 수행 인증 검사 결과가 도착했어요! 🫣",
            String.format("투두 내용 : %s\n검사 결과 : %s", dailyTodo.getContent(), dailyTodo.getStatusDescription()),
            "REVIEW"
        );
    }

    public List<DailyTodoCertificationDto> findAllTodoCertificationsForReview(final Long memberId) {
        final Member reviewer = getMember(memberId);
        final List<DailyTodoCertification> dailyTodoCertificationsForReview = dailyTodoCertificationRepository.findAllByReviewerAndDailyTodo_StatusAndDailyTodo_ChallengeGroup_Status(
            reviewer,
            DailyTodoStatus.REVIEW_PENDING,
            ChallengeGroupStatus.RUNNING);

        return dailyTodoCertificationsForReview.stream()
            .map(DailyTodoCertificationDto::from)
            .toList();
    }
}
