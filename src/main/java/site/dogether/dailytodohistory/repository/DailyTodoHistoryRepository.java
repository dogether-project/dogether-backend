package site.dogether.dailytodohistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.dailytodohistory.entity.DailyTodoHistory;

public interface DailyTodoHistoryRepository extends JpaRepository<DailyTodoHistory, Long> {
}
