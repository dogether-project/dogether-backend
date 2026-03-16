package site.dogether.reminder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.member.entity.Member;
import site.dogether.reminder.entity.DailyTodoActivityReminderType;
import site.dogether.reminder.entity.TodoActivityReminderHistory;

public interface TodoActivityReminderHistoryRepository extends JpaRepository<TodoActivityReminderHistory, Long> {

    boolean existsByMemberAndDailyTodoAndReminderType(
        Member member,
        DailyTodo dailyTodo,
        DailyTodoActivityReminderType reminderType
    );
}
