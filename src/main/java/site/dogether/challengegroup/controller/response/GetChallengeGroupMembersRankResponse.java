package site.dogether.challengegroup.controller.response;

import site.dogether.challengegroup.service.dto.ChallengeGroupMemberOverviewDto;
import site.dogether.dailytodohistory.entity.DailyTodoHistoryReadStatus;

import java.util.List;

public record GetChallengeGroupMembersRankResponse(List<Data> ranking) {

    public static GetChallengeGroupMembersRankResponse from(List<ChallengeGroupMemberOverviewDto> challengeGroupMemberOverview) {
        final List<Data> data = challengeGroupMemberOverview.stream()
            .map(overView -> new Data(
                overView.memberId(),
                overView.rank(),
                overView.profileImageUrl(),
                overView.name(),
                overView.historyReadStatus(),
                overView.achievementRate()
            ))
            .toList();
        return new GetChallengeGroupMembersRankResponse(data);
    }

    record Data(
        Long memberId,
        int rank,
        String profileImageUrl,
        String name,
        DailyTodoHistoryReadStatus historyReadStatus,
        int achievementRate
    ) {}
}
