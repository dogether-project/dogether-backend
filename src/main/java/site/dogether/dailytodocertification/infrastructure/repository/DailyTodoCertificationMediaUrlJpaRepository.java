package site.dogether.dailytodocertification.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.dailytodocertification.infrastructure.entity.DailyTodoCertificationJpaEntity;
import site.dogether.dailytodocertification.infrastructure.entity.DailyTodoCertificationMediaUrlJpaEntity;

import java.util.List;

public interface DailyTodoCertificationMediaUrlJpaRepository extends JpaRepository<DailyTodoCertificationMediaUrlJpaEntity, Long> {

    List<DailyTodoCertificationMediaUrlJpaEntity> findAllByDailyTodoCertification(DailyTodoCertificationJpaEntity dailyTodoCertification);
}
