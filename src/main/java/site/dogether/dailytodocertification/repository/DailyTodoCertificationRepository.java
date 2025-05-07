package site.dogether.dailytodocertification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.member.entity.Member;

import java.util.List;
import java.util.Optional;

public interface DailyTodoCertificationRepository extends JpaRepository<DailyTodoCertification, Long> {

    boolean existsByDailyTodo_StatusAndReviewer(DailyTodoStatus dailytodoStatus, Member reviewer);

    List<DailyTodoCertification> findAllByReviewerAndDailyTodo_StatusAndDailyTodo_ChallengeGroup_Status(
        Member reviewer,
        DailyTodoStatus dailyTodoStatus,
        ChallengeGroupStatus challengeGroupStatus
    );

    Optional<DailyTodoCertification> findByDailyTodo(final DailyTodo dailyTodo);

    List<DailyTodoCertification> findAllByReviewer(Member member);

    List<DailyTodoCertification> findAllByDailyTodo_Member(Member member);

    List<DailyTodoCertification> findAllByDailyTodo_MemberAndDailyTodo_Status(Member member, DailyTodoStatus status);
}
