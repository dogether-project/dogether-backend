package site.dogether.memberactivity.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.member.entity.Member;
import site.dogether.memberactivity.entity.DailyTodoStats;

public interface DailyTodoStatsRepository extends JpaRepository<DailyTodoStats, Long> {

    Optional<DailyTodoStats> findByMember(Member member);
}
