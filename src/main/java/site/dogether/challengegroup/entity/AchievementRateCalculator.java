package site.dogether.challengegroup.entity;

import site.dogether.dailytodo.entity.DailyTodos;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AchievementRateCalculator {

    public static int calculate(
        final DailyTodos todos,
        final LocalDateTime challengeGroupJoinedAt,
        final LocalDate challengeGroupStartAt,
        final LocalDate challengeGroupEndAt
    ) {
        if (todos.isEmpty()) {
            return 0;
        }

        final int totalTodoCount = todos.totalCount();
        final int certificatedTodoCount = todos.certificatedCount();
        final int approvedTodoCount = todos.approvedCount();

        final double certificationRate = (double) certificatedTodoCount / totalTodoCount;
        final double approvalRate = (double) approvedTodoCount / certificatedTodoCount;
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
