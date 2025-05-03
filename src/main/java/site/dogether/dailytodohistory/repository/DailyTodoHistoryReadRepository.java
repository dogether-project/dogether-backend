package site.dogether.dailytodohistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.dailytodohistory.entity.DailyTodoHistoryRead;

public interface DailyTodoHistoryReadRepository extends JpaRepository<DailyTodoHistoryRead, Long> {
}
