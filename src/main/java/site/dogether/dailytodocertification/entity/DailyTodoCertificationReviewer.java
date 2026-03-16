package site.dogether.dailytodocertification.entity;

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
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodocertification.exception.InvalidDailyTodoCertificationReviewerException;
import site.dogether.member.entity.Member;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo_certification_reviewer")
@Entity
public class DailyTodoCertificationReviewer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "daily_todo_certification_id",  nullable = false, updatable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private DailyTodoCertification dailyTodoCertification;

    @JoinColumn(name = "reviewer_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member reviewer;

    public DailyTodoCertificationReviewer(final DailyTodoCertification dailyTodoCertification, final Member reviewer) {
        this(null, dailyTodoCertification, reviewer);
    }

    public DailyTodoCertificationReviewer(
        final Long id,
        final DailyTodoCertification dailyTodoCertification,
        final Member reviewer
    ) {
        validateDailyTodoCertification(dailyTodoCertification);
        validateReviewer(reviewer, dailyTodoCertification);

        this.id = id;
        this.dailyTodoCertification = dailyTodoCertification;
        this.reviewer = reviewer;
    }

    private void validateDailyTodoCertification(final DailyTodoCertification dailyTodoCertification) {
        if (dailyTodoCertification == null) {
            throw new InvalidDailyTodoCertificationReviewerException("데일리 투두 인증으로 null을 입력할 수 없습니다.");
        }
    }

    private void validateReviewer(final Member reviewer, final DailyTodoCertification dailyTodoCertification) {
        final DailyTodo dailyTodo = dailyTodoCertification.getDailyTodo();
        if (dailyTodo.isWriter(reviewer)) {
            throw new InvalidDailyTodoCertificationReviewerException(String.format("데일리 투두 인증 검사자로 투두 작성자 본인을 지정할 수 없습니다. (%s) (%s)", dailyTodoCertification, reviewer));
        }
    }

    public Long getReviewerId() {
        return reviewer.getId();
    }
}
