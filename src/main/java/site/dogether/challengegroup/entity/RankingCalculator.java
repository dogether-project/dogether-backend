package site.dogether.challengegroup.entity;

import site.dogether.dailytodo.entity.MyTodoSummary;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RankingCalculator {

    public static int calculateAchievementRate(
            final MyTodoSummary myTodoSummary,
            final LocalDateTime joinedAt,
            final LocalDate groupStartAt,
            final LocalDate groupEndAt
    ) {
        final int totalTodoCount = myTodoSummary.calculateTotalTodoCount();
        final int totalCertificatedCount = myTodoSummary.calculateTotalCertificatedCount();
        final int totalApprovedCount = myTodoSummary.calculateTotalApprovedCount();

        if (totalTodoCount == 0) {
            return 0;
        }

        double certificationRate = (double) totalCertificatedCount / totalTodoCount;
        double approvalRate = (double) totalApprovedCount / totalCertificatedCount;
        double participationRate = calculateParticipationRate(joinedAt, groupStartAt, groupEndAt);

        double score = certificationRate + approvalRate + participationRate;
        return (int) Math.floor((score / 3.0) * 100);
    }

    private static double calculateParticipationRate(
            final LocalDateTime joinedAt,
            final LocalDate groupStartAt,
            final LocalDate groupEndAt
    ) {
        LocalDate now = LocalDate.now();
        long totalDays = Duration.between(groupStartAt.atStartOfDay(), groupEndAt.atStartOfDay()).toDays();
        long participatedDays = Duration.between(joinedAt, now.atStartOfDay()).toDays();

        if (totalDays <= 0) {
            return 0;
        }

        return Math.min((double) participatedDays / totalDays, 1.0);
    }
}
