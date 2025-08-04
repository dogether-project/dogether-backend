package site.dogether.challengegroup.controller.v1.dto.response;

import site.dogether.challengegroup.service.dto.ChallengeGroupMemberOverviewDto;
import site.dogether.dailytodohistory.entity.DailyTodoHistoryReadStatus;

import java.util.List;

public record GetChallengeGroupMembersRankApiResponseV1(List<Data> ranking) {

    public static GetChallengeGroupMembersRankApiResponseV1 from(List<ChallengeGroupMemberOverviewDto> challengeGroupMemberOverview) {
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
        return new GetChallengeGroupMembersRankApiResponseV1(data);
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
