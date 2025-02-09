package site.dogether.dailytodocertification.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.dailytodo.domain.DailyTodoStatus;
import site.dogether.dailytodocertification.infrastructure.entity.DailyTodoCertificationJpaEntity;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;

public interface DailyTodoCertificationJpaRepository extends JpaRepository<DailyTodoCertificationJpaEntity, Long> {

    boolean existsByDailyTodo_StatusAndReviewer(DailyTodoStatus dailytodoStatus, MemberJpaEntity reviewer);
}
