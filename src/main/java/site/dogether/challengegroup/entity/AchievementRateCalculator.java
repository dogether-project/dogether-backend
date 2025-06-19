package site.dogether.challengegroup.entity;

import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationCount;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AchievementRateCalculator {

    private static final int DAILY_TODO_LIMIT = 10;

    public static int calculate(
        List<DailyTodo> dailyTodos,
        final LocalDate challengeGroupStartAt,
        final LocalDate challengeGroupEndAt,
        final DailyTodoCertificationCount dailyTodoCertificationCount
    ) {

        if (dailyTodos.isEmpty()) {
            return 0;
        }

        if (!challengeGroupEndAt.isAfter(challengeGroupStartAt)) {
            return 0;
        }

        final long totalGroupDuration = Duration.between(challengeGroupStartAt.atStartOfDay(), challengeGroupEndAt.atStartOfDay()).toDays();

        final long groupTotalTodoLimit = DAILY_TODO_LIMIT * totalGroupDuration;

        final int totalTodoCount = dailyTodos.size();
        final int certificatedCount = dailyTodoCertificationCount.getTotalCount();
        final int approvedCount = dailyTodoCertificationCount.getApprovedCount();

        final double todoWriteRate = (double) totalTodoCount / groupTotalTodoLimit;
        final double certificationRate = (double) certificatedCount / groupTotalTodoLimit;
        final double approvalRate = (double) approvedCount / groupTotalTodoLimit;
        final double participationRate = calculateParticipationRate(dailyTodos, totalGroupDuration);

        final double score = todoWriteRate + certificationRate + approvalRate + participationRate;

        return (int) Math.floor((score / 4.0) * 100);
    }

    private static double calculateParticipationRate(
        final List<DailyTodo> dailyTodos,
        final long totalGroupDuration
    ) {
        final int activeWritingDaysCount = dailyTodos.stream()
            .map(todo -> todo.getWrittenAt().toLocalDate())
            .collect(Collectors.toSet())
            .size();

        return Math.min((double) activeWritingDaysCount / totalGroupDuration, 1.0);
    }
}
