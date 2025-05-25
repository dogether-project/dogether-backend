package site.dogether.challengegroup.entity;

import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationCount;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AchievementRateCalculator {

    public static int calculate(
        List<DailyTodo> dailyTodos,
        final LocalDateTime challengeGroupJoinedAt,
        final LocalDate challengeGroupStartAt,
        final LocalDate challengeGroupEndAt,
        final DailyTodoCertificationCount dailyTodoCertificationCount
    ) {
        if (dailyTodos.isEmpty()) {
            return 0;
        }
        // TODO: 추후 해당 로직 리팩토링 필요
        final int totalTodoCount = dailyTodos.size();
        final int certificatedCount = dailyTodoCertificationCount.getTotalCount();
        final int approvedCount = dailyTodoCertificationCount.getApprovedCount();

        final double certificationRate = (double) certificatedCount / totalTodoCount;
        final double approvalRate = (double) approvedCount / certificatedCount;
        final double participationRate = calculateParticipationRate(challengeGroupJoinedAt, challengeGroupStartAt, challengeGroupEndAt);

        final double score = certificationRate + approvalRate + participationRate;
        return (int) Math.floor((score / 3.0) * 100);
    }

    private static double calculateParticipationRate(
        final LocalDateTime challengeGroupJoinedAt,
        final LocalDate challengeGroupStartAt,
        final LocalDate challengeGroupEndAt
    ) {
        final LocalDate nowDate = LocalDate.now();
        final long totalGroupDuration = Duration.between(challengeGroupStartAt.atStartOfDay(), challengeGroupEndAt.atStartOfDay()).toDays();
        final long participatedDays = Duration.between(challengeGroupJoinedAt, nowDate.atStartOfDay()).toDays();

        if (totalGroupDuration <= 0) {
            return 0;
        }

        return Math.min((double) participatedDays / totalGroupDuration, 1.0);
    }
}
