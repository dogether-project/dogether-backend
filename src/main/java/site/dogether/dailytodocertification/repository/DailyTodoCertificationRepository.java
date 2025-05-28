package site.dogether.dailytodocertification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus;
import site.dogether.member.entity.Member;

import java.util.List;
import java.util.Optional;

public interface DailyTodoCertificationRepository extends JpaRepository<DailyTodoCertification, Long> {

    Optional<DailyTodoCertification> findByDailyTodo(final DailyTodo dailyTodo);

    List<DailyTodoCertification> findAllByDailyTodo_Member(Member member);

    List<DailyTodoCertification> findAllByDailyTodo_MemberAndReviewStatus(Member member, DailyTodoCertificationReviewStatus reviewStatus);

    @Query("""
    SELECT dtc
    FROM DailyTodoCertification dtc
    JOIN DailyTodoCertificationReviewer reviewer ON dtc = reviewer.dailyTodoCertification
    JOIN dtc.dailyTodo dt
    JOIN dt.challengeGroup cg
    WHERE
        reviewer.reviewer = :reviewer AND
        dtc.reviewStatus = 'REVIEW_PENDING' AND
        cg.status IN ('RUNNING', 'D_DAY')
    """)
    List<DailyTodoCertification> findAllCertificationsToReview(@Param("reviewer") Member reviewer);

    @Query("""
    SELECT new site.dogether.dailytodocertification.repository.DailyTodoCertificationCount(
        COUNT(dtc),
        SUM(CASE WHEN dtc.reviewStatus = site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus.APPROVE THEN 1 ELSE 0 END),
        SUM(CASE WHEN dtc.reviewStatus = site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus.REJECT THEN 1 ELSE 0 END)
    )
    FROM DailyTodoCertification dtc
    WHERE
        dtc.dailyTodo.challengeGroup = :challengeGroup AND
        dtc.dailyTodo.member = :member
    """)
    DailyTodoCertificationCount countDailyTodoCertification(
        @Param("challengeGroup") ChallengeGroup challengeGroup,
        @Param("member") Member member
    );
}
