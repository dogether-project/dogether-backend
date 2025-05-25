package site.dogether.dailytodo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus;
import site.dogether.member.entity.Member;

import java.time.LocalDateTime;
import java.util.List;

public interface DailyTodoRepository extends JpaRepository<DailyTodo, Long> {

    List<DailyTodo> findAllByWrittenAtBetweenAndChallengeGroupAndMember(
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        ChallengeGroup challengeGroup,
        Member member
    );

    List<DailyTodo> findAllByChallengeGroupAndMember(
        ChallengeGroup joiningGroupEntity,
        Member member
    );

    List<DailyTodo> findAllByChallengeGroupAndMemberAndWrittenAtBetween(
        ChallengeGroup challengeGroup,
        Member member,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
    );

    List<DailyTodo> findAllByChallengeGroupAndMemberAndStatusAndWrittenAtBetween(
        ChallengeGroup challengeGroup,
        Member member,
        DailyTodoStatus status,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
    );

    boolean existsByChallengeGroupAndMemberAndWrittenAtBetween(
        ChallengeGroup challengeGroup,
        Member member,
        LocalDateTime startOfDay,
        LocalDateTime endOfDay
    );

    @Query("""
    SELECT new site.dogether.dailytodo.repository.DailyTodoAndDailyTodoCertification(dt, dtc)
    FROM DailyTodo dt
    JOIN DailyTodoCertification dtc ON dt = dtc.dailyTodo
    WHERE
        dt.challengeGroup = :challengeGroup AND
        dt.member = :member AND
        dt.writtenAt BETWEEN :startDate AND :endDate AND
        dtc.reviewStatus = :reviewResult
    """)
    List<DailyTodoAndDailyTodoCertification> findAllDailyTodoAndCertificationByReviewResult(
        @Param("challengeGroup") ChallengeGroup challengeGroup,
        @Param("member") Member member,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("reviewResult") DailyTodoCertificationReviewStatus reviewStatus
    );

    @Query("""
    SELECT new site.dogether.dailytodo.repository.DailyTodoAndDailyTodoCertification(dt, dtc)
    FROM DailyTodo dt
    JOIN DailyTodoCertification dtc ON dt = dtc.dailyTodo
    WHERE
        dt.challengeGroup = :challengeGroup AND
        dt.member = :member AND
        dt.writtenAt BETWEEN :startDate AND :endDate
    """)
    List<DailyTodoAndDailyTodoCertification> findAllDailyTodoAndCertification(
        @Param("challengeGroup") ChallengeGroup challengeGroup,
        @Param("member") Member member,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
