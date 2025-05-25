package site.dogether.memberactivity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus;
import site.dogether.dailytodocertification.exception.InvalidDailyTodoCertificationReviewStatusException;
import site.dogether.member.entity.Member;

import static site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus.APPROVE;
import static site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus.REJECT;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo_stats")
@Entity
public class DailyTodoStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "certificated_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int certificatedCount = 0;

    @Column(name = "approved_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int approvedCount = 0;

    @Column(name = "rejected_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int rejectedCount = 0;

    public DailyTodoStats(final Member member) {
        this.member = member;
    }

    public DailyTodoStats(
            final Long id,
            final Member member,
            final int certificatedCount,
            final int approvedCount,
            final int rejectedCount
    ) {
        this.id = id;
        this.member = member;
        this.certificatedCount = certificatedCount;
        this.approvedCount = approvedCount;
        this.rejectedCount = rejectedCount;
    }

    public void increaseCertificatedCount() {
        this.certificatedCount += 1;
    }

    public void moveCertificatedToResult(final DailyTodoCertificationReviewStatus dailyTodoCertificationReviewResult) {
        if(dailyTodoCertificationReviewResult == APPROVE) {
            this.approvedCount += 1;
            return;
        }

        if(dailyTodoCertificationReviewResult == REJECT) {
            this.rejectedCount += 1;
            return;
        }

        throw new InvalidDailyTodoCertificationReviewStatusException(String.format("유효하지 않은 데일리 투두 인증 검사 결과 입니다. (%s)", dailyTodoCertificationReviewResult));
    }
}
