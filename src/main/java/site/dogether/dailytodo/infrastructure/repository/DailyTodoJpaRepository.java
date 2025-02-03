package site.dogether.dailytodo.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.dailytodo.infrastructure.entity.DailyTodoJpaEntity;

public interface DailyTodoJpaRepository extends JpaRepository<DailyTodoJpaEntity, Long> {
}
