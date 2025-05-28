package site.dogether.dailytodohistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.dailytodohistory.entity.DailyTodoHistory;
import site.dogether.dailytodohistory.entity.DailyTodoHistoryRead;
import site.dogether.member.entity.Member;

public interface DailyTodoHistoryReadRepository extends JpaRepository<DailyTodoHistoryRead, Long> {

    boolean existsByMemberAndDailyTodoHistory(Member member, DailyTodoHistory dailyTodoHistory);

    void deleteAllByDailyTodoHistory(DailyTodoHistory dailyTodoHistory);
}
