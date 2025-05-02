package site.dogether.dailytodo.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.member.entity.Member;

public interface DailyTodoRepository extends JpaRepository<DailyTodo, Long> {

    List<DailyTodo> findAllByCreatedAtBetweenAndChallengeGroupAndMember(
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        ChallengeGroup challengeGroup,
        Member member
    );

    List<DailyTodo> findAllByChallengeGroupAndMember(
        ChallengeGroup joiningGroupEntity,
        Member member
    );

    List<DailyTodo> findAllByMemberAndCreatedAtBetween(
        Member member,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
    );

    List<DailyTodo> findAllByMemberAndCreatedAtBetweenAndStatus(
        Member member,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        DailyTodoStatus status
    );

    boolean existsByChallengeGroupAndMemberAndCreatedAtBetween(
        ChallengeGroup challengeGroup,
        Member member,
        LocalDateTime startOfDay,
        LocalDateTime endOfDay
    );
  
    List<DailyTodo> findAllByMember(Member member);
}
