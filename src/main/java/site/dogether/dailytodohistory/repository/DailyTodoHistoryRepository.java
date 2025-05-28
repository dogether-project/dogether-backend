package site.dogether.dailytodohistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodohistory.entity.DailyTodoHistory;
import site.dogether.member.entity.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DailyTodoHistoryRepository extends JpaRepository<DailyTodoHistory, Long> {

    Optional<DailyTodoHistory> findByDailyTodo(DailyTodo dailyTodo);

    List<DailyTodoHistory> findAllByDailyTodo_ChallengeGroupAndDailyTodo_MemberAndDailyTodo_WrittenAtBetweenOrderByEventTimeAsc(
        ChallengeGroup challengeGroup,
        Member member,
        LocalDateTime start,
        LocalDateTime end
    );
}
