package site.dogether.dailytodo.entity;

import site.dogether.challengegroup.service.dto.ChallengeGroupMemberRankInfoDto;
import site.dogether.challengegroup.service.dto.RankDto;

import java.util.List;
import java.util.stream.IntStream;

public class GroupTodoSummary {

    private final List<ChallengeGroupMemberRankInfoDto> membersTodoSummary;

    public GroupTodoSummary(final List<ChallengeGroupMemberRankInfoDto> membersTodoSummary) {
        this.membersTodoSummary = membersTodoSummary;
    }

    public List<RankDto> getRanks() {
        final List<RankDto> allRanking = membersTodoSummary.stream()
                .map(RankDto::from)
                .sorted((o1, o2) -> (o2.getAchievementRate() - o1.getAchievementRate()))
                .toList();

        IntStream.range(0, allRanking.size())
                .forEach(i -> allRanking.get(i).setRank(i + 1));

        return allRanking;
    }
}
