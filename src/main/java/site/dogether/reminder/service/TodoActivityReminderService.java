package site.dogether.reminder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.exception.DailyTodoNotFoundException;
import site.dogether.dailytodo.repository.DailyTodoRepository;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewer;
import site.dogether.dailytodocertification.exception.DailyTodoCertificationNotFoundException;
import site.dogether.dailytodocertification.exception.DailyTodoCertificationReviewerNotFoundException;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationRepository;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationReviewerRepository;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;
import site.dogether.notification.service.NotificationService;
import site.dogether.reminder.entity.DailyTodoActivityReminderType;
import site.dogether.reminder.entity.TodoActivityReminderHistory;
import site.dogether.reminder.repository.TodoActivityReminderHistoryRepository;

import static site.dogether.reminder.entity.DailyTodoActivityReminderType.TODO_CERTIFICATION;
import static site.dogether.reminder.entity.DailyTodoActivityReminderType.TODO_CERTIFICATION_REVIEW;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TodoActivityReminderService {

    private final MemberRepository memberRepository;
    private final DailyTodoRepository dailyTodoRepository;
    private final DailyTodoCertificationRepository dailyTodoCertificationRepository;
    private final DailyTodoCertificationReviewerRepository dailyTodoCertificationReviewerRepository;
    private final TodoActivityReminderHistoryRepository todoActivityReminderHistoryRepository;
    private final NotificationService notificationService;

    public void sendReminder(final Long requesterId, final Long todoId, final DailyTodoActivityReminderType reminderType) {
        final Member requester = getMember(requesterId);

        if (reminderType == TODO_CERTIFICATION) {
            sendTodoCertificationReminder(requester, todoId);
        }
        sendTodoCertificationReviewReminder(requester, todoId);
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 id입니다. (%d)", memberId)));
    }

    private void sendTodoCertificationReminder(final Member requester, final Long todoId) {
        final DailyTodo targetTodo = getDailyTodo(todoId);
        if (canRequestCertification(requester, targetTodo)) {
            // send
            notificationService.sendNotification(
                targetTodo.getWriterId(),
                "데일리 투두 인증 요청 푸시 알림 제목 (임시)",
                "데일리 투두 인증 요청 푸시 알림 본문 (임시)",
                "TODO_CERTIFICATION_REMINDER"
            );
            final TodoActivityReminderHistory todoActivityReminderHistory = new TodoActivityReminderHistory(targetTodo, requester, TODO_CERTIFICATION);
            todoActivityReminderHistoryRepository.save(todoActivityReminderHistory);
        }
    }

    private DailyTodo getDailyTodo(final Long dailyTodoId) {
        return dailyTodoRepository.findById(dailyTodoId)
            .orElseThrow(() -> new DailyTodoNotFoundException(String.format("존재하지 않는 데일리 투두 id입니다. (%d)", dailyTodoId)));
    }

    private void sendTodoCertificationReviewReminder(final Member requester, final Long todoId) {
        final DailyTodoCertification targetTodoCertification = getDailyTodoCertification(todoId);
        if (canRequestCertificationReview(requester, targetTodoCertification)) {
            // send
            final DailyTodoCertificationReviewer dailyTodoCertificationReviewer = getDailyTodoCertificationReviewer(targetTodoCertification);
            notificationService.sendNotification(
                dailyTodoCertificationReviewer.getReviewerId(),
                "데일리 투두 인증 검사 요청 푸시 알림 제목 (임시)",
                "데일리 투두 인증 검사 요청 푸시 알림 본문 (임시)",
                "TODO_CERTIFICATION_REVIEW_REMINDER"
            );
            final TodoActivityReminderHistory todoActivityReminderHistory = new TodoActivityReminderHistory(targetTodoCertification.getDailyTodo(), requester, TODO_CERTIFICATION_REVIEW);
            todoActivityReminderHistoryRepository.save(todoActivityReminderHistory);
        }
    }

    private DailyTodoCertification getDailyTodoCertification(final Long todoId) {
        return dailyTodoCertificationRepository.findByDailyTodo(getDailyTodo(todoId))
            .orElseThrow(() -> new DailyTodoCertificationNotFoundException(String.format("존재하지 않는 데일리 투두 인증 입니다. (%d)", todoId)));
    }

    private DailyTodoCertificationReviewer getDailyTodoCertificationReviewer(final DailyTodoCertification dailyTodoCertification) {
        return dailyTodoCertificationReviewerRepository.findByDailyTodoCertification(dailyTodoCertification)
            .orElseThrow(() -> new DailyTodoCertificationReviewerNotFoundException(String.format("해당 투두 인증에 배정된 검사자가 존재하지 않습니다. (%s)", dailyTodoCertification)));
    }

    public boolean canRequestCertification(final Member requester, final DailyTodo dailyTodo) {
        return !dailyTodo.isWriter(requester) &&
            dailyTodo.isCertifyPending() &&
            dailyTodo.isWrittenToday() &&
            !todoActivityReminderHistoryRepository.existsByMemberAndDailyTodoAndReminderType(requester, dailyTodo, TODO_CERTIFICATION);
    }

    public boolean canRequestCertificationReview(final Member requester, final DailyTodoCertification dailyTodoCertification) {
        return dailyTodoCertification.isReviewPending() &&
            !dailyTodoCertificationReviewerRepository.existsByDailyTodoCertificationAndReviewer(dailyTodoCertification, requester) &&
            !todoActivityReminderHistoryRepository.existsByMemberAndDailyTodoAndReminderType(requester, dailyTodoCertification.getDailyTodo(), TODO_CERTIFICATION_REVIEW);
    }
}
