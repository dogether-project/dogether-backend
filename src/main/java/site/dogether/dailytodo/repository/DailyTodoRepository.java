package site.dogether.dailytodo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.member.entity.Member;

import java.time.LocalDateTime;
import java.util.List;

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
}
