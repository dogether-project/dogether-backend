package site.dogether.dailytodocertification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodocertification.exception.InvalidDailyTodoCertificationException;
import site.dogether.member.entity.Member;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo_certification")
@Entity
public class DailyTodoCertification extends BaseEntity {

    public static final int MAXIMUM_ALLOWED_CONTENT_LENGTH = 40;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "daily_todo_id", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private DailyTodo dailyTodo;

    @JoinColumn(name = "reviewer_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member reviewer;

    @Column(name = "content", length = 200, nullable = false)
    private String content;

    @Column(name = "media_url", length = 500, nullable = false)
    private String mediaUrl;

    public DailyTodoCertification(
        final DailyTodo dailyTodo,
        final Member reviewer,
        final String content,
        final String mediaUrl
    ) {
        this(null, dailyTodo, reviewer, content, mediaUrl);
    }

    public DailyTodoCertification(
        final Long id,
        final DailyTodo dailyTodo,
        final Member reviewer,
        final String content,
        final String mediaUrl
    ) {
        validateDailyTodo(dailyTodo);
        validateReviewer(reviewer, dailyTodo);
        validateContent(content);
        validateMediaUrl(mediaUrl);

        this.id = id;
        this.dailyTodo = dailyTodo;
        this.reviewer = reviewer;
        this.content = content;
        this.mediaUrl = mediaUrl;
    }

    private void validateDailyTodo(final DailyTodo dailyTodo) {
        if (dailyTodo == null) {
            throw new InvalidDailyTodoCertificationException("데일리 투두 인증 생성에 데일리 투두로 null을 입력할 수 없습니다.");
        }
    }

    private void validateReviewer(final Member reviewer, final DailyTodo dailyTodo) {
        if (dailyTodo != null && dailyTodo.isWriter(reviewer)) {
            throw new InvalidDailyTodoCertificationException(String.format("데일리 투두 인증 검사자로 본인을 지정할 수 없습니다. (%s)", reviewer));
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

    public boolean checkReviewer(final Member target) {
        return reviewer.getId().equals(target.getId());
    }

    public ChallengeGroup getChallengeGroup() {
        return dailyTodo.getChallengeGroup();
    }

    public String getReviewerName() {
        return reviewer.getName();
    }

    public String getDoerName() {
        return dailyTodo.getMemberName();
    }

    public String getDailyTodoContent() {
        return dailyTodo.getContent();
    }
}
