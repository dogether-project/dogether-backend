package site.dogether.memberactivity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.member.entity.Member;

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
}
