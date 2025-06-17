package site.dogether.dailytodocertification.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.exception.AlreadyReviewedDailyTodoCertificationException;
import site.dogether.dailytodocertification.exception.InvalidDailyTodoCertificationException;
import site.dogether.member.entity.Member;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus.REJECT;
import static site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus.REVIEW_PENDING;

@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo_certification")
@Entity
public class DailyTodoCertification extends BaseEntity {

    public static final int MAXIMUM_ALLOWED_CONTENT_LENGTH = 500;
    public static final int MAXIMUM_ALLOWED_REVIEW_FEEDBACK_LENGTH = 800;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "daily_todo_id", nullable = false, updatable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private DailyTodo dailyTodo;

    @Column(name = "content", length = 500, nullable = false, updatable = false)
    private String content;

    @Column(name = "media_url", length = 500, nullable = false, updatable = false)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", length = 20, nullable = false)
    private DailyTodoCertificationReviewStatus reviewStatus;

    @Column(name = "review_feedback", length = 800)
    private String reviewFeedback;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ToString.Exclude
    @OneToOne(mappedBy = "dailyTodoCertification", cascade = CascadeType.REMOVE)
    private DailyTodoCertificationReviewer dailyTodoCertificationReviewer;

    public DailyTodoCertification(
        final DailyTodo dailyTodo,
        final String content,
        final String mediaUrl
    ) {
        this(null, dailyTodo, content, mediaUrl, REVIEW_PENDING, null, LocalDateTime.now());
    }

    public DailyTodoCertification(
        final Long id,
        final DailyTodo dailyTodo,
        final String content,
        final String mediaUrl,
        final DailyTodoCertificationReviewStatus reviewStatus,
        final String reviewFeedback,
        final LocalDateTime createdAt
    ) {
        validateDailyTodo(dailyTodo);
        validateContent(content);
        validateMediaUrl(mediaUrl);
        validateReviewStatus(reviewStatus);
        validateReviewFeedback(reviewStatus, reviewFeedback);
        validateCreatedAt(createdAt);

        this.id = id;
        this.dailyTodo = dailyTodo;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.reviewStatus = reviewStatus;
        this.reviewFeedback = reviewFeedback;
        this.createdAt = createdAt;
    }

    private void validateDailyTodo(final DailyTodo dailyTodo) {
        if (dailyTodo == null) {
            throw new InvalidDailyTodoCertificationException("데일리 투두 인증 생성에 데일리 투두로 null을 입력할 수 없습니다.");
        }
    }

    private void validateContent(final String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidDailyTodoCertificationException(String.format("데일리 투두 인증 내용으로 null 혹은 공백을 입력할 수 없습니다. (%s)", content));
        }

        if (content.length() > MAXIMUM_ALLOWED_CONTENT_LENGTH) {
            throw new InvalidDailyTodoCertificationException(String.format("데일리 투두 인증 내용은 %d자 이하만 입력할 수 있습니다. (%d) (%s)", MAXIMUM_ALLOWED_CONTENT_LENGTH, content.length(), content));
        }
    }

    private void validateMediaUrl(final String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isBlank()) {
            throw new InvalidDailyTodoCertificationException(String.format("데일리 투두 인증 미디어 url로 null 혹은 공백을 입력할 수 없습니다. (%s)", mediaUrl));
        }
    }

    private void validateReviewStatus(final DailyTodoCertificationReviewStatus reviewStatus) {
        if (reviewStatus == null) {
            throw new InvalidDailyTodoCertificationException("데일리 투두 인증 상태로 null을 입력할 수 없습니다.");
        }
    }

    private void validateReviewFeedback(final DailyTodoCertificationReviewStatus reviewStatus, final String reviewFeedback) {
        if (reviewStatus == REJECT && (reviewFeedback == null || reviewFeedback.isBlank())) {
            throw new InvalidDailyTodoCertificationException(String.format("데일리 투두 인증 검사 상태가 노인정이면 검사 피드백으로 null 혹은 공백을 입력할 수 없습니다. (%s)", reviewFeedback));
        }

        if (reviewStatus == REJECT && reviewFeedback.length() > MAXIMUM_ALLOWED_REVIEW_FEEDBACK_LENGTH) {
            throw new InvalidDailyTodoCertificationException(String.format("검사 피드백은 %d자 이하만 입력할 수 있습니다. (%d) (%s)", MAXIMUM_ALLOWED_REVIEW_FEEDBACK_LENGTH, reviewFeedback.length(), reviewFeedback));
        }
    }

    private void validateCreatedAt(final LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new InvalidDailyTodoCertificationException("데일리 투두 인증 생성일로 null을 입력할 수 없습니다.");
        }
    }

    public void review(final DailyTodoCertificationReviewStatus reviewResult, final String reviewFeedback) {
        validateReviewStatus(reviewStatus);
        validateReviewFeedback(reviewResult, reviewFeedback);
        validateDailyTodoCertificationNotReviewed();

        this.reviewStatus = reviewResult;
        this.reviewFeedback = reviewFeedback;
    }

    private void validateDailyTodoCertificationNotReviewed() {
        if (reviewStatus != REVIEW_PENDING) {
            throw new AlreadyReviewedDailyTodoCertificationException(String.format("이미 검사된 데일리 투두 인증 입니다. (%s)", this));
        }
    }

    public Long getId() {
        return id;
    }

    public DailyTodo getDailyTodo() {
        return dailyTodo;
    }

    public String getContent() {
        return content;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public DailyTodoCertificationReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public Optional<String> findReviewFeedback() {
        return Optional.ofNullable(reviewFeedback);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ChallengeGroup getChallengeGroup() {
        return dailyTodo.getChallengeGroup();
    }

    public Member getDailyTodoWriter() {
        return dailyTodo.getMember();
    }

    public Long getDailyTodoWriterId() {
        return dailyTodo.getWriterId();
    }

    public String getDailyTodoWriterName() {
        return dailyTodo.getMemberName();
    }

    public String getDailyTodoContent() {
        return dailyTodo.getContent();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        final DailyTodoCertification that = (DailyTodoCertification) object;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
