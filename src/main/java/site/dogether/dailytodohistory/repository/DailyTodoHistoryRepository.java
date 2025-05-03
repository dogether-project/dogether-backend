package site.dogether.dailytodohistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.dailytodohistory.entity.DailyTodoHistory;
import site.dogether.member.entity.Member;

import java.time.LocalDateTime;
import java.util.List;

public interface DailyTodoHistoryRepository extends JpaRepository<DailyTodoHistory, Long> {

    List<DailyTodoHistory> findAllByChallengeGroupAndMemberAndEventAtBetween(
        ChallengeGroup challengeGroup,
        Member member,
        LocalDateTime start,
        LocalDateTime end
    );
}
