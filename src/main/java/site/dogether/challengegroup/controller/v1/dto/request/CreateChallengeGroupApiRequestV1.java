package site.dogether.challengegroup.controller.v1.dto.request;

import site.dogether.challengegroup.entity.ChallengeGroupDurationOption;
import site.dogether.challengegroup.entity.ChallengeGroupStartAtOption;

public record CreateChallengeGroupApiRequestV1(
    String groupName,
    int maximumMemberCount,
    String startAt,
    int duration
) {
    public ChallengeGroupStartAtOption challengeGroupStartAtOption() {
        return ChallengeGroupStartAtOption.from(startAt);
    }

    public ChallengeGroupDurationOption challengeGroupDurationOption() {
        return ChallengeGroupDurationOption.from(duration);
    }
}
