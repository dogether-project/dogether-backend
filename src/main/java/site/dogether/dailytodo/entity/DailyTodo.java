package site.dogether.dailytodo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.member.entity.Member;

import java.time.LocalDateTime;

import static site.dogether.dailytodo.entity.DailyTodoStatus.*;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo")
@Entity
public class DailyTodo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "challenge_group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChallengeGroup challengeGroup;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "content", length = 30, nullable = false)
    private String content;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private DailyTodoStatus status;

    @Column(name = "reject_reason", length = 500, nullable = true)
    private String rejectReason;

    public static DailyTodo create(
        final String content,
        final Member member,
        final ChallengeGroup challengeGroup
    ) {
        return new DailyTodo(
            null,
            challengeGroup,
            member,
            content,
            CERTIFY_PENDING,
            null
        );
    }

    public DailyTodo(
        final Long id,
        final ChallengeGroup challengeGroup,
        final Member member,
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

    // TODO : Daily Todo 필드 검종 메서드와 단위 테스트 추가

    public void changeStatusReviewPending() {
        this.status = REVIEW_PENDING;
    }

    public boolean checkOwner(final Member member) {
        return this.member.getId().equals(member.getId());
    }

    public boolean isCertifyPending() {
        return status == CERTIFY_PENDING;
    }

    public boolean isApproved() {
        return status == APPROVE;
    }

    public boolean isRejected() {
        return status == REJECT;
    }

    public boolean createdToday() {
        return createdAt.toLocalDate()
            .isEqual(LocalDateTime.now().toLocalDate());
    }

    public void review(final DailyTodoStatus reviewResult, final String rejectReason) {
        this.status = reviewResult;
        this.rejectReason = rejectReason;
    }

    public String getStatusDescription() {
        return status.getDescription();
    }

    public Long getMemberId() {
        return member.getId();
    }
}
