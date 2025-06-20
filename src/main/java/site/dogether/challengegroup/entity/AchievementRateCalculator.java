package site.dogether.challengegroup.entity;

import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationCount;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AchievementRateCalculator {

    private static final int DAILY_TODO_LIMIT = 10;

    private static final int TODO_WRITE_WEIGHT = 1;
    private static final int CERTIFICATION_WEIGHT = 2;
    private static final int APPROVAL_WEIGHT = 3;
    private static final int PARTICIPATION_WEIGHT = 4;
    private static final int TOTAL_WEIGHT = TODO_WRITE_WEIGHT + CERTIFICATION_WEIGHT + APPROVAL_WEIGHT + PARTICIPATION_WEIGHT;

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

        final double score = (todoWriteRate * TODO_WRITE_WEIGHT) + (certificationRate * CERTIFICATION_WEIGHT) + (approvalRate * APPROVAL_WEIGHT) + (participationRate * PARTICIPATION_WEIGHT);

        return (int) Math.floor((score / TOTAL_WEIGHT) * 100);
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
