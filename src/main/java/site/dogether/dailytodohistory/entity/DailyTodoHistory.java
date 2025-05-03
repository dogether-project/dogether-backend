package site.dogether.dailytodohistory.entity;

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
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.member.entity.Member;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo_history")
@Entity
public class DailyTodoHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "challenge_group_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ChallengeGroup challengeGroup;

    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "daily_todo_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private DailyTodo dailyTodo;

    @Column(name = "history_type", length = 15, nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private DailyTodoHistoryType historyType;

    @Column(name = "event_at", nullable = false, updatable = false)
    private LocalDateTime event_at;

    public DailyTodoHistory(
        final ChallengeGroup challengeGroup,
        final Member member,
        final DailyTodo dailyTodo,
        final DailyTodoHistoryType historyType
    ) {
        this.id = null;
        this.challengeGroup = challengeGroup;
        this.member = member;
        this.dailyTodo = dailyTodo;
        this.historyType = historyType;
        this.event_at = LocalDateTime.now();
    }

    public DailyTodoHistory(
        final Long id,
        final ChallengeGroup challengeGroup,
        final Member member,
        final DailyTodo dailyTodo,
        final DailyTodoHistoryType historyType,
        final LocalDateTime event_at
    ) {
        this.id = id;
        this.challengeGroup = challengeGroup;
        this.member = member;
        this.dailyTodo = dailyTodo;
        this.historyType = historyType;
        this.event_at = event_at;
    }
}
