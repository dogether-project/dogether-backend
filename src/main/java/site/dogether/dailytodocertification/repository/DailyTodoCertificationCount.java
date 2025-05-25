package site.dogether.dailytodocertification.repository;

public record DailyTodoCertificationCount(
    Long totalCount,
    Long approvedCount,
    Long rejectedCount
) {}
