package site.dogether.dailytodocertification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.exception.NotRunningChallengeGroupException;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationMediaUrl;
import site.dogether.dailytodocertification.exception.DailyTodoCertificationNotFoundException;
import site.dogether.dailytodocertification.exception.NotDailyTodoCertificationReviewerException;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationMediaUrlRepository;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationRepository;
import site.dogether.dailytodocertification.service.dto.DailyTodoCertificationDto;
import site.dogether.member.entity.Member;
import site.dogether.member.service.MemberService;
import site.dogether.notification.service.NotificationService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DailyTodoCertificationService {

    private final DailyTodoCertificationRepository dailyTodoCertificationRepository;
    private final DailyTodoCertificationMediaUrlRepository dailyTodoCertificationMediaUrlRepository;
    private final NotificationService notificationService;
    private final MemberService memberService;

    @Transactional
    public void reviewDailyTodoCertification(
        final Long memberId,
        final Long dailyTodoCertificationId,
        final String reviewResult,
        final String rejectReason
    ) {
        final DailyTodoCertification dailyTodoCertification = dailyTodoCertificationRepository.findById(dailyTodoCertificationId)
            .orElseThrow(() -> new DailyTodoCertificationNotFoundException(String.format("해당 id의 데일리 투두 수행 인증 정보가 존재하지 않습니다. (input : %d)", dailyTodoCertificationId)));
        final Member reviewer = memberService.getMember(memberId);
        checkDailyTodoCertificationReviewer(dailyTodoCertification, reviewer);
        checkChallengeGroupIsRunning(dailyTodoCertification.getChallengeGroup());

        final DailyTodo dailyTodo = dailyTodoCertification.getDailyTodo();
        dailyTodo.review(DailyTodoStatus.valueOf(reviewResult), rejectReason);

        final String notificationTitle = "투두 수행 인증 검사 결과가 도착했어! 🫣";
        final String notificationMessage = String.format("투두 내용 : %s\n검사 결과 : %s", dailyTodo.getContent(), dailyTodo.getStatusDescription());
        notificationService.sendNotification(dailyTodo.getMemberId(), notificationTitle, notificationMessage, "REVIEW");
    }

    private void checkDailyTodoCertificationReviewer(final DailyTodoCertification dailyTodoCertification, final Member reviewer) {
        if (!dailyTodoCertification.checkReviewer(reviewer)) {
            throw new NotDailyTodoCertificationReviewerException(String.format("해당 데일리 투두 수행 인증의 검사자가 아닙니다. (certification : %s) (member : %s)", dailyTodoCertification, reviewer));
        }
    }

    private void checkChallengeGroupIsRunning(final ChallengeGroup challengeGroup) {
        if (!challengeGroup.isRunning()) {
            throw new NotRunningChallengeGroupException(String.format("현재 진행중인 챌린지 그룹이 아닙니다. (%s)", challengeGroup));
        }
    }

    public List<DailyTodoCertificationDto> findAllTodoCertificationsForReview(final Long memberId) {
        final Member reviewer = memberService.getMember(memberId);
        final List<DailyTodoCertification> dailyTodoCertificationsForReview = dailyTodoCertificationRepository.findAllByReviewerAndDailyTodo_StatusAndDailyTodo_ChallengeGroup_Status(
            reviewer,
            DailyTodoStatus.REVIEW_PENDING,
            ChallengeGroupStatus.RUNNING);

        return dailyTodoCertificationsForReview.stream()
            .map(dailyTodoCertification -> DailyTodoCertificationDto.from(
                dailyTodoCertification,
                findAllDailyTodoCertificationMediaUrlValuesByDailyTodoCertification(dailyTodoCertification)))
            .toList();
    }

    private List<String> findAllDailyTodoCertificationMediaUrlValuesByDailyTodoCertification(final DailyTodoCertification dailyTodoCertification) {
        return dailyTodoCertificationMediaUrlRepository.findAllByDailyTodoCertification(dailyTodoCertification)
            .stream()
            .map(DailyTodoCertificationMediaUrl::getValue)
            .toList();
    }

    public DailyTodoCertificationDto findTodoCertificationById(final Long todoCertificationId) {
        final DailyTodoCertification dailyTodoCertification = dailyTodoCertificationRepository.findById(todoCertificationId)
            .orElseThrow(() -> new DailyTodoCertificationNotFoundException(String.format(("해당 id의 데일리 투두 수행 인증 정보가 존재하지 않습니다. (input : %d)" + todoCertificationId))));

        return DailyTodoCertificationDto.from(
            dailyTodoCertification,
            findAllDailyTodoCertificationMediaUrlValuesByDailyTodoCertification(dailyTodoCertification)
        );
    }
}
