package site.dogether.challengegroup.service.dto;

import site.dogether.dailytodohistory.entity.DailyTodoHistoryReadStatus;

public record ChallengeGroupMemberOverviewDto(
    Long memberId,
    int rank,
    String profileImageUrl,
    String name,
    DailyTodoHistoryReadStatus historyReadStatus,
    int achievementRate
) {}
