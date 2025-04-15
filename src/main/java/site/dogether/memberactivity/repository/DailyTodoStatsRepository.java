package site.dogether.memberactivity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.memberactivity.entity.DailyTodoStats;

public interface DailyTodoStatsRepository extends JpaRepository<DailyTodoStats, Long> {
}
