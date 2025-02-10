package site.dogether.challengegroup.domain;

public enum ChallengeGroupStartAtOption {

    TODAY,
    TOMORROW
    ;

    public static ChallengeGroupStartAtOption from(final String startAt) {
        return ChallengeGroupStartAtOption.valueOf(startAt.toUpperCase());
    }
}
