package site.dogether.dailytodocertification.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.dailytodocertification.infrastructure.entity.DailyTodoCertificationJpaEntity;

public interface DailyTodoCertificationJpaRepository extends JpaRepository<DailyTodoCertificationJpaEntity, Long> {
}
