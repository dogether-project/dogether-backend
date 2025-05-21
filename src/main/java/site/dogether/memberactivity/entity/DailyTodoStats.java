package site.dogether.memberactivity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodo.exception.InvalidDailyTodoStatusException;
import site.dogether.member.entity.Member;
import site.dogether.memberactivity.exception.InvalidDailyTodoStatsException;

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

    public DailyTodoStats(
            final Long id,
            final Member member,
            final int certificatedCount,
            final int approvedCount,
            final int rejectedCount
    ) {
        validateMember(member);

        this.id = id;
        this.member = member;
        this.certificatedCount = certificatedCount;
        this.approvedCount = approvedCount;
        this.rejectedCount = rejectedCount;
    }

    public DailyTodoStats(final Member member) {
        validateMember(member);

        this.member = member;
    }

    private void validateMember(final Member member) {
        if(member == null) {
            throw new InvalidDailyTodoStatsException("데일리 투두 통계 멤버로 null을 입력할 수 없습니다.");
        }
    }

    public void increaseCertificatedCount() {
        this.certificatedCount += 1;
    }

    public void moveCertificatedToResult(final DailyTodoStatus result) {
        if(result.equals(DailyTodoStatus.APPROVE)) {
            this.approvedCount += 1;
            return;
        }

        if(result.equals(DailyTodoStatus.REJECT)) {
            this.rejectedCount += 1;
            return;
        }

        throw new InvalidDailyTodoStatusException(String.format("존재하지 않는 데일리 투두 상태입니다. (%s)", result));
    }
}
