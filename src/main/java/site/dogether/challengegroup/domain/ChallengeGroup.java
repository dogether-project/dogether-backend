package site.dogether.challengegroup.domain;

import lombok.Getter;

@Getter
public class ChallengeGroup {

    private final Long id;
    private final String name;
    private final int maximumMemberCount;
    private final ChallengeGroupStartAtOption startAtOption;
    private final ChallengeGroupDurationOption durationOption;
    private final int maximumTodoCount;
    private final ChallengeGroupStatus status;

    public ChallengeGroup(
            final String name,
            final int maximumMemberCount,
            final String startAt,
            final int durationOption,
            final int maximumTodoCount
    ) {
        this.id = null;
        this.name = name;
        this.maximumMemberCount = maximumMemberCount;
        this.startAtOption = setStartAtOption(startAt);
        this.durationOption = setDurationOption(durationOption);
        this.maximumTodoCount = maximumTodoCount;
        this.status = setStatus();
    }

    private ChallengeGroupStartAtOption setStartAtOption(final String startAt) {
        return ChallengeGroupStartAtOption.from(startAt);
    }

    private ChallengeGroupDurationOption setDurationOption(final int durationOption) {
        return ChallengeGroupDurationOption.from(durationOption);
    }

    private ChallengeGroupStatus setStatus() {
        if (startAtOption == ChallengeGroupStartAtOption.TOMORROW) {
            return ChallengeGroupStatus.READY;
        }
        return ChallengeGroupStatus.RUNNING;
    }

}
