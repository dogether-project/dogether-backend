package site.dogether.dailytodocertification.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.dailytodocertification.infrastructure.entity.DailyTodoCertificationMediaUrlJpaEntity;

public interface DailyTodoCertificationMediaUrlJpaRepository extends JpaRepository<DailyTodoCertificationMediaUrlJpaEntity, Long> {
}
