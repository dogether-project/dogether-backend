package site.dogether.dailytodo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodo.exception.InvalidDailyTodoException;
import site.dogether.dailytodo.exception.InvalidReviewResultException;
import site.dogether.dailytodo.exception.NotCertifyPendingDailyTodoException;
import site.dogether.dailytodo.exception.NotCreatedTodayDailyTodoException;
import site.dogether.dailytodo.exception.NotDailyTodoWriterException;
import site.dogether.dailytodo.exception.NotReviewPendingDailyTodoException;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.exception.NotDailyTodoCertificationReviewerException;
import site.dogether.member.entity.Member;
import site.dogether.memberactivity.entity.DailyTodoStats;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static site.dogether.dailytodo.entity.DailyTodoStatus.*;

@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo")
@Entity
public class DailyTodo extends BaseEntity {

    public static final int MAXIMUM_ALLOWED_CONTENT_LENGTH = 20;
    public static final int MAXIMUM_ALLOWED_REVIEW_FEEDBACK_LENGTH = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "challenge_group_id", updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ChallengeGroup challengeGroup;

    @JoinColumn(name = "member_id", updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "content", length = 30, nullable = false, updatable = false)
    private String content;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private DailyTodoStatus status;

    @Column(name = "review_feedback", length = 500)
    private String reviewFeedback;

    @Column(name = "written_at", nullable = false, updatable = false)
    private LocalDateTime writtenAt;

    public DailyTodo(
        final ChallengeGroup challengeGroup,
        final Member member,
        final String content
    ) {
        this(
            null,
            challengeGroup,
            member,
            content,
            CERTIFY_PENDING,
            null,
            LocalDateTime.now()
        );
    }

    public DailyTodo(
        final Long id,
        final ChallengeGroup challengeGroup,
        final Member member,
        final String content,
        final DailyTodoStatus status,
        final String reviewFeedback,
        final LocalDateTime writtenAt
    ) {
        validateChallengeGroup(challengeGroup);
        validateMember(member);
        validateContent(content);
        validateStatus(status);
        validateReviewFeedback(status, reviewFeedback);
        validateWrittenAt(writtenAt);

        this.id = id;
        this.challengeGroup = challengeGroup;
        this.member = member;
        this.content = content;
        this.status = status;
        this.reviewFeedback = reviewFeedback;
        this.writtenAt = writtenAt;
    }

    private void validateChallengeGroup(final ChallengeGroup challengeGroup) {
        if (challengeGroup == null) {
            throw new InvalidDailyTodoException("데일리 투두 챌린지 그룹으로 null을 입력할 수 없습니다.");
        }
    }

    private void validateMember(final Member member) {
        if (member == null) {
            throw new InvalidDailyTodoException("데일리 투두 작성자로 null을 입력할 수 없습니다.");
        }
    }

    private void validateContent(final String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidDailyTodoException(String.format("데일리 투두 내용으로 null 혹은 공백을 입력할 수 없습니다. (%s)", content));
        }

        if (content.length() > MAXIMUM_ALLOWED_CONTENT_LENGTH) {
            throw new InvalidDailyTodoException(String.format("데일리 투두 내용은 %d자 이하만 입력할 수 있습니다. (%d) (%s)", MAXIMUM_ALLOWED_CONTENT_LENGTH, content.length(), content));
        }
    }

    private void validateStatus(final DailyTodoStatus status) {
        if (status == null) {
            throw new InvalidDailyTodoException("데일리 투두 상태로 null을 입력할 수 없습니다.");
        }
    }

    private void validateReviewFeedback(final DailyTodoStatus status, final String reviewFeedback) {
        if (status.isReviewResultStatus() && (reviewFeedback == null || reviewFeedback.isBlank())) {
            throw new InvalidDailyTodoException(String.format("검사 피드백으로 null 혹은 공백을 입력할 수 없습니다. (%s)", reviewFeedback));
        }

        if (status.isReviewResultStatus() && reviewFeedback.length() > MAXIMUM_ALLOWED_REVIEW_FEEDBACK_LENGTH) {
            throw new InvalidDailyTodoException(String.format("검사 피드백은 %d자 이하만 입력할 수 있습니다. (%d) (%s)", MAXIMUM_ALLOWED_REVIEW_FEEDBACK_LENGTH, reviewFeedback.length(), reviewFeedback));
        }
    }

    private void validateWrittenAt(final LocalDateTime writtenAt) {
        if (writtenAt == null) {
            throw new InvalidDailyTodoException("데일리 투두 작성일로 null을 입력할 수 없습니다.");
        }
    }

    public boolean isCertifyPending() {
        return status == CERTIFY_PENDING;
    }

    public boolean isWriter(final Member target) {
        return member.equals(target);
    }

    public DailyTodoCertification certify(
        final Member writer,
        final Member reviewer,
        final String certifyContent,
        final String certifyMediaUrl,
        final DailyTodoStats dailyTodoStats
    ) {
        validateWriter(writer);
        validateStatusIsCertifyPending();
        validateWrittenToday();

        final DailyTodoCertification dailyTodoCertification = new DailyTodoCertification(this, reviewer, certifyContent, certifyMediaUrl);
        status = REVIEW_PENDING;

        dailyTodoStats.increaseCertificatedCount();

        return dailyTodoCertification;
    }

    private void validateWriter(final Member target) {
        if (!isWriter(target)) {
            throw new NotDailyTodoWriterException(String.format("데일리 투두 작성자 외에는 투두 인증을 생성할 수 없습니다. (%s) (%s)", this, target));
        }
    }

    private void validateStatusIsCertifyPending() {
        if (status != CERTIFY_PENDING) {
            throw new NotCertifyPendingDailyTodoException(String.format("인증 대기 상태가 아닌 데일리 투두는 인증을 생성할 수 없습니다. (%s)", this));
        }
    }

    private void validateWrittenToday() {
        final boolean writtenToday = writtenAt.toLocalDate().isEqual(LocalDate.now());
        if (!writtenToday) {
            throw new NotCreatedTodayDailyTodoException(String.format("데일리 투두가 작성된 당일에만 투두 인증을 생성할 수 있습니다. (%s)", this));
        }
    }

    public void review(
            final Member reviewer,
            final DailyTodoCertification dailyTodoCertification,
            final DailyTodoStatus reviewResult,
            final String reviewFeedback,
            final DailyTodoStats dailyTodoStats
            ) {
        validateReviewer(reviewer, dailyTodoCertification);
        validateStatusIsReviewPending();
        validateReviewResult(reviewResult);
        validateReviewFeedback(reviewResult, reviewFeedback);

        this.status = reviewResult;
        this.reviewFeedback = reviewFeedback;

        dailyTodoStats.moveCertificatedToResult(reviewResult);
    }

    private void validateReviewer(final Member reviewer, final DailyTodoCertification dailyTodoCertification) {
        if (!dailyTodoCertification.isReviewer(reviewer)) {
            throw new NotDailyTodoCertificationReviewerException(String.format("해당 투두 인증 검사자 외 멤버는 검사를 수행할 수 없습니다. (%s) (%s)", reviewer, dailyTodoCertification));
        }
    }

    private void validateStatusIsReviewPending() {
        if (this.status != REVIEW_PENDING) {
            throw new NotReviewPendingDailyTodoException(String.format("검사 대기가 아닌 투두는 검사를 수행할 수 없습니다. (%s)", this));
        }
    }

    private void validateReviewResult(final DailyTodoStatus reviewResult) {
        if (!reviewResult.isReviewResultStatus()) {
            throw new InvalidReviewResultException(String.format("검사 결과는 인정 혹은 노인정만 입력할 수 있습니다. (%s) (%s)", reviewResult, this));
        }
    }

    public boolean isApproved() {
        return status == APPROVE;
    }

    public boolean isRejected() {
        return status == REJECT;
    }

    public boolean isCertified() {
        return status.isCertificatedStatus();
    }

    public Long getId() {
        return id;
    }

    public ChallengeGroup getChallengeGroup() {
        return challengeGroup;
    }

    public String getContent() {
        return content;
    }

    public DailyTodoStatus getStatus() {
        return status;
    }

    public Optional<String> getReviewFeedback() {
        return Optional.ofNullable(reviewFeedback);
    }

    public String getStatusDescription() {
        return status.getDescription();
    }

    public Member getMember() {
        return member;
    }

    public Long getWriterId() {
        return member.getId();
    }

    public String getMemberName() {
        return member.getName();
    }

    public LocalDateTime getWrittenAt() {
        return writtenAt;
    }
}
