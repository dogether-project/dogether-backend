package site.dogether.dailytodocertification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewer;
import site.dogether.member.entity.Member;

public interface DailyTodoCertificationReviewerRepository extends JpaRepository<DailyTodoCertificationReviewer, Long> {

    boolean existsByDailyTodoCertificationAndReviewer(DailyTodoCertification dailyTodoCertification, Member reviewer);
}
