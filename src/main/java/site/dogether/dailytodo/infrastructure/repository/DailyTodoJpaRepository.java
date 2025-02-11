package site.dogether.dailytodo.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupJpaEntity;
import site.dogether.dailytodo.infrastructure.entity.DailyTodoJpaEntity;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface DailyTodoJpaRepository extends JpaRepository<DailyTodoJpaEntity, Long> {

    List<DailyTodoJpaEntity> findAllByCreatedAtBetweenAndChallengeGroupAndMember(
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        ChallengeGroupJpaEntity challengeGroup,
        MemberJpaEntity member
    );
}
