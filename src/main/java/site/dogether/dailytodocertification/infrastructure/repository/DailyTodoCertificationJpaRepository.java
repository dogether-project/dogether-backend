package site.dogether.dailytodocertification.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.domain.ChallengeGroupStatus;
import site.dogether.dailytodo.domain.DailyTodoStatus;
import site.dogether.dailytodocertification.infrastructure.entity.DailyTodoCertificationJpaEntity;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;

import java.util.List;

public interface DailyTodoCertificationJpaRepository extends JpaRepository<DailyTodoCertificationJpaEntity, Long> {

    boolean existsByDailyTodo_StatusAndReviewer(DailyTodoStatus dailytodoStatus, MemberJpaEntity reviewer);

    List<DailyTodoCertificationJpaEntity> findAllByReviewerAndDailyTodo_StatusAndDailyTodo_ChallengeGroup_Status(
        MemberJpaEntity reviewer,
        DailyTodoStatus dailyTodoStatus,
        ChallengeGroupStatus challengeGroupStatus
    );
}
