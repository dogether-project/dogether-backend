package site.dogether.dailytodocertification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.service.ChallengeGroupPolicy;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.exception.DailyTodoNotFoundException;
import site.dogether.dailytodo.repository.DailyTodoRepository;
import site.dogether.dailytodo.service.ReviewerPicker;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewer;
import site.dogether.dailytodocertification.exception.DailyTodoCertificationNotFoundException;
import site.dogether.dailytodocertification.exception.NotDailyTodoCertificationReviewerException;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationRepository;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationReviewerRepository;
import site.dogether.dailytodocertification.service.dto.DailyTodoCertificationDto;
import site.dogether.dailytodohistory.service.DailyTodoHistoryService;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;
import site.dogether.memberactivity.entity.DailyTodoStats;
import site.dogether.memberactivity.exception.DailyTodoStatsNotFoundException;
import site.dogether.memberactivity.repository.DailyTodoStatsRepository;
import site.dogether.notification.service.NotificationService;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DailyTodoCertificationService {

    private final MemberRepository memberRepository;
    private final DailyTodoRepository dailyTodoRepository;
    private final DailyTodoCertificationRepository dailyTodoCertificationRepository;
    private final DailyTodoStatsRepository dailyTodoStatsRepository;
    private final DailyTodoCertificationReviewerRepository dailyTodoCertificationReviewerRepository;
    private final ReviewerPicker reviewerPicker;
    private final DailyTodoHistoryService dailyTodoHistoryService;
    private final NotificationService notificationService;
    private final ChallengeGroupPolicy challengeGroupPolicy;

    @Transactional
    public void certifyDailyTodo(
        final Long memberId,
        final Long dailyTodoId,
        final String certifyContent,
        final String certifyMediaUrl
    ) {
        final Member writer = getMember(memberId);
        final DailyTodo dailyTodo = getDailyTodo(dailyTodoId);
        final ChallengeGroup challengeGroup = dailyTodo.getChallengeGroup();
        final DailyTodoStats dailyTodoStats = getDailyTodoStats(writer);

        challengeGroupPolicy.validateMemberIsInChallengeGroup(challengeGroup, writer);
        challengeGroupPolicy.validateChallengeGroupIsRunning(challengeGroup);

        final DailyTodoCertification dailyTodoCertification = createDailyTodoCertification(dailyTodo, writer, certifyContent, certifyMediaUrl, dailyTodoStats);
        final Optional<Member> reviewer = pickDailyTodoCertificationReviewer(challengeGroup, writer, dailyTodoCertification);
        dailyTodoHistoryService.updateDailyTodoHistory(dailyTodo);

        reviewer.ifPresent(target -> sendNotificationToReviewer(target, writer, dailyTodo));
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 id입니다. (%d)", memberId)));
    }

    private DailyTodo getDailyTodo(final Long dailyTodoId) {
        return dailyTodoRepository.findById(dailyTodoId)
            .orElseThrow(() -> new DailyTodoNotFoundException(String.format("존재하지 않는 데일리 투두 id입니다. (%d)", dailyTodoId)));
    }

    private DailyTodoStats getDailyTodoStats(final Member dailyTodoWriter) {
        return dailyTodoStatsRepository.findByMember(dailyTodoWriter)
            .orElseThrow(() -> new DailyTodoStatsNotFoundException(String.format("사용자의 데일리 투두 통계가 존재하지 않습니다. (%s)", dailyTodoWriter)));
    }

    private DailyTodoCertification createDailyTodoCertification(
        final DailyTodo dailyTodo,
        final Member dailyTodoWriter,
        final String certifyContent,
        final String certifyMediaUrl,
        final DailyTodoStats dailyTodoStats
    ) {
        final DailyTodoCertification dailyTodoCertification = dailyTodo.certify(dailyTodoWriter, certifyContent, certifyMediaUrl, dailyTodoStats);
        return dailyTodoCertificationRepository.save(dailyTodoCertification);
    }

    private Optional<Member> pickDailyTodoCertificationReviewer(
        final ChallengeGroup challengeGroup,
        final Member dailyTodoWriter,
        final DailyTodoCertification dailyTodoCertification
    ) {
        final Optional<Member> pickedMember = reviewerPicker.pickReviewerInChallengeGroup(challengeGroup, dailyTodoWriter);
        pickedMember.ifPresent(picked -> {
            final DailyTodoCertificationReviewer dailyTodoCertificationReviewer = new DailyTodoCertificationReviewer(dailyTodoCertification, picked);
            dailyTodoCertificationReviewerRepository.save(dailyTodoCertificationReviewer);
        });

        return pickedMember;
    }

    private void sendNotificationToReviewer(
        final Member reviewer,
        final Member writer,
        final DailyTodo dailyTodo
    ) {
        if (reviewer == null) {
            return;
        }

        notificationService.sendNotification(
            reviewer.getId(),
            String.format("%s님의 투두 인증 검사자로 선정되었습니다.", writer.getName()),
            String.format("투두 내용 : %s",dailyTodo.getContent()),
            "CERTIFICATION"
        );
    }

    @Transactional
    public void reviewDailyTodoCertification(
        final Long reviewerId,
        final Long dailyTodoCertificationId,
        final String reviewResultValue,
        final String reviewFeedback
    ) {
        final Member reviewer = getMember(reviewerId);
        final DailyTodoCertification dailyTodoCertification = getDailyTodoCertification(dailyTodoCertificationId);
        final DailyTodoStats dailyTodoStats = getDailyTodoStats(dailyTodoCertification.getDailyTodoWriter());
        final DailyTodoCertificationReviewStatus reviewResult = DailyTodoCertificationReviewStatus.convertReviewResultStatusByValue(reviewResultValue);

        validateReviewer(dailyTodoCertification, reviewer);

        dailyTodoCertification.review(reviewResult, reviewFeedback);
        dailyTodoStats.moveCertificatedToResult(reviewResult);
        dailyTodoHistoryService.updateDailyTodoHistory(dailyTodoCertification.getDailyTodo());

        sendReviewResultNotificationToDailyTodoWriter(dailyTodoCertification.getDailyTodoWriterId(), dailyTodoCertification.getDailyTodoContent(), reviewResult);
    }

    private DailyTodoCertification getDailyTodoCertification(final Long dailyTodoCertificationId) {
        return dailyTodoCertificationRepository.findById(dailyTodoCertificationId)
            .orElseThrow(() -> new DailyTodoCertificationNotFoundException(String.format("존재하지 않는 데일리 투두 인증 id입니다. (%d)", dailyTodoCertificationId)));
    }

    private void validateReviewer(final DailyTodoCertification dailyTodoCertification, final Member reviewer) {
        if (!dailyTodoCertificationReviewerRepository.existsByDailyTodoCertificationAndReviewer(dailyTodoCertification, reviewer)) {
            throw new NotDailyTodoCertificationReviewerException(String.format("해당 투두 인증 검사자 외 멤버는 검사를 수행할 수 없습니다. (%s) (%s)", dailyTodoCertification, reviewer));
        }
    }

    private void sendReviewResultNotificationToDailyTodoWriter(
        final Long dailyTodoWriterId,
        final String dailyTodoContent,
        final DailyTodoCertificationReviewStatus dailyTodoCertificationReviewResult
    ) {
        notificationService.sendNotification(
            dailyTodoWriterId,
            "투두 수행 인증 검사 결과가 도착했어요! 🫣",
            String.format("투두 내용 : %s\n검사 결과 : %s", dailyTodoContent, dailyTodoCertificationReviewResult.getDescription()),
            "REVIEW"
        );
    }

    public List<DailyTodoCertificationDto> findAllTodoCertificationsToReviewer(final Long reviewerId) {
        final Member reviewer = getMember(reviewerId);
        final List<DailyTodoCertification> dailyTodoCertifications = dailyTodoCertificationRepository.findAllCertificationsToReview(reviewer);

        return dailyTodoCertifications.stream()
            .map(DailyTodoCertificationDto::from)
            .toList();
    }
}
