package site.dogether.dailytodohistory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import site.dogether.member.entity.Member;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo_history_read")
@Entity
public class DailyTodoHistoryRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "daily_todo_history_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private DailyTodoHistory dailyTodoHistory;

    public DailyTodoHistoryRead(
        final Member member,
        final DailyTodoHistory dailyTodoHistory
    ) {
        this.id = null;
        this.member = member;
        this.dailyTodoHistory = dailyTodoHistory;
    }

    public DailyTodoHistoryRead(
        final Long id,
        final Member member,
        final DailyTodoHistory dailyTodoHistory
    ) {
        this.id = id;
        this.member = member;
        this.dailyTodoHistory = dailyTodoHistory;
    }
}
