package site.dogether.dailytodo.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupJpaEntity;
import site.dogether.common.audit.entity.BaseTimeEntity;
import site.dogether.dailytodo.domain.DailyTodo;
import site.dogether.dailytodo.domain.DailyTodoStatus;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo")
@Entity
public class DailyTodoJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "challenge_group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChallengeGroupJpaEntity challengeGroup;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private MemberJpaEntity member;

    @Column(name = "content", length = 30, nullable = false)
    private String content;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private DailyTodoStatus status;

    @Column(name = "reject_reason", length = 500, nullable = true)
    private String rejectReason;

    public DailyTodoJpaEntity(
        final DailyTodo dailyTodo,
        final ChallengeGroupJpaEntity challengeGroup,
        final MemberJpaEntity member
    ) {
        this(
            null,
            challengeGroup,
            member,
            dailyTodo.getContent(),
            dailyTodo.getStatus(),
            dailyTodo.getRejectReason().get()
        );
    }

    public DailyTodoJpaEntity(
        final Long id,
        final ChallengeGroupJpaEntity challengeGroup,
        final MemberJpaEntity member,
        final String content,
        final DailyTodoStatus status,
        final String rejectReason
    ) {
        this.id = id;
        this.challengeGroup = challengeGroup;
        this.member = member;
        this.content = content;
        this.status = status;
        this.rejectReason = rejectReason;
    }

    public DailyTodo toDomain() {
        return new DailyTodo(
            id,
            content,
            status,
            rejectReason,
            getCreatedAt(),
            member.toDomain(),
            challengeGroup.toDomain()
        );
    }

    public void changeStatusReviewPending() {
        this.status = DailyTodoStatus.REVIEW_PENDING;
    }

    public void changeReviewResult(final DailyTodo dailyTodo) {
        this.status = dailyTodo.getStatus();
        this.rejectReason = dailyTodo.getRejectReason().get();
    }
}
