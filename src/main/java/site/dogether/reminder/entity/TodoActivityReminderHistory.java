package site.dogether.reminder.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.member.entity.Member;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "todo_activity_reminder_history")
@Entity
public class TodoActivityReminderHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "daily_todo_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private DailyTodo dailyTodo;

    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "reminder_type", length = 100, nullable = false)
    @Enumerated(EnumType.STRING)
    private DailyTodoActivityReminderType reminderType;

    public TodoActivityReminderHistory(
        final DailyTodo dailyTodo,
        final Member member,
        final DailyTodoActivityReminderType reminderType
    ) {
        this(null, dailyTodo, member, reminderType);
    }

    public TodoActivityReminderHistory(
        final Long id,
        final DailyTodo dailyTodo,
        final Member member,
        final DailyTodoActivityReminderType reminderType
    ) {
        this.id = id;
        this.dailyTodo = dailyTodo;
        this.member = member;
        this.reminderType = reminderType;
    }
}
