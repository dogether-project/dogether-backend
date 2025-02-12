package site.dogether.dailytodo.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupJpaEntity;
import site.dogether.dailytodo.infrastructure.entity.DailyTodoJpaEntity;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;

public interface DailyTodoJpaRepository extends JpaRepository<DailyTodoJpaEntity, Long> {

    List<DailyTodoJpaEntity> findAllByCreatedAtBetweenAndChallengeGroupAndMember(
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        ChallengeGroupJpaEntity challengeGroup,
        MemberJpaEntity member
    );

    List<DailyTodoJpaEntity> findAllByChallengeGroupAndMember(
        ChallengeGroupJpaEntity joiningGroupEntity,
        MemberJpaEntity memberJpaEntity
    );
}
