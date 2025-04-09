package site.dogether.dailytodocertification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationMediaUrl;

import java.util.List;

public interface DailyTodoCertificationMediaUrlRepository extends JpaRepository<DailyTodoCertificationMediaUrl, Long> {

    List<DailyTodoCertificationMediaUrl> findAllByDailyTodoCertification(DailyTodoCertification dailyTodoCertification);
}
