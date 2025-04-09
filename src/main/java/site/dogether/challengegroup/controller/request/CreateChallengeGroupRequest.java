package site.dogether.challengegroup.controller.request;

import site.dogether.challengegroup.entity.ChallengeGroupDurationOption;
import site.dogether.challengegroup.entity.ChallengeGroupStartAtOption;

public record CreateChallengeGroupRequest(
    String name,
    int maximumMemberCount,
    String startAt,
    int durationOption,
    int maximumTodoCount
) {
    public ChallengeGroupStartAtOption challengeGroupStartAtOption() {
        return ChallengeGroupStartAtOption.from(startAt);
    }

    public ChallengeGroupDurationOption challengeGroupDurationOption() {
        return ChallengeGroupDurationOption.from(durationOption);
    }
}
