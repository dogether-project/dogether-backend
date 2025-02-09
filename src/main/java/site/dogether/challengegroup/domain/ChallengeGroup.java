package site.dogether.challengegroup.domain;

import lombok.Getter;

@Getter
public class ChallengeGroup {

    private final Long id;
    private final String name;
    private final int maximumMemberCount;
    private final ChallengeGroupStartAtOption startAtOption;
    private final ChallengeGroupDurationOption durationOption;
    private final ChallengeGroupStatus status;

    public ChallengeGroup(
            final String name,
            final int maximumMemberCount,
            final ChallengeGroupStartAtOption startAtOption,
            final ChallengeGroupDurationOption durationOption
    ) {
        this(null, name, maximumMemberCount, startAtOption, durationOption);
    }

    public ChallengeGroup(final Long id,
                          final String name,
                          final int maximumMemberCount,
                          final ChallengeGroupStartAtOption startAtOption,
                          final ChallengeGroupDurationOption durationOption) {
        this.id = id;
        this.name = name;
        this.maximumMemberCount = maximumMemberCount;
        this.startAtOption = startAtOption;
        this.durationOption = durationOption;
        this.status = ChallengeGroupStatus.READY;
    }
}
