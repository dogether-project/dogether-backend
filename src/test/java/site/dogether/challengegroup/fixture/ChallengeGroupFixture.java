package site.dogether.challengegroup.fixture;

import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.entity.JoinCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ChallengeGroupFixture {

    private static final String DEFAULT_NAME = "테스트 챌린지 그룹";
    private static final int DEFAULT_MAXIMUM_MEMBER_COUNT = 10;
    private static final int DEFAULT_DURATION_DAYS = 7;

    public static ChallengeGroup create() {
        return new ChallengeGroup(
                DEFAULT_NAME,
                DEFAULT_MAXIMUM_MEMBER_COUNT,
                LocalDate.now(),
                LocalDate.now().plusDays(DEFAULT_DURATION_DAYS),
                JoinCode.generate(),
                LocalDateTime.now()
        );
    }

    public static ChallengeGroup create(final String name) {
        return new ChallengeGroup(
                name,
                DEFAULT_MAXIMUM_MEMBER_COUNT,
                LocalDate.now(),
                LocalDate.now().plusDays(DEFAULT_DURATION_DAYS),
                JoinCode.generate(),
                LocalDateTime.now()
        );
    }

    public static ChallengeGroup create(final int maximumMemberCount) {
        return new ChallengeGroup(
                DEFAULT_NAME,
                maximumMemberCount,
                LocalDate.now(),
                LocalDate.now().plusDays(DEFAULT_DURATION_DAYS),
                JoinCode.generate(),
                LocalDateTime.now()
        );
    }

    public static ChallengeGroup create(final ChallengeGroupStatus status) {
        final LocalDate startAt;
        final LocalDate endAt;

        switch (status) {
            case READY -> {
                startAt = LocalDate.now().plusDays(1);
                endAt = startAt.plusDays(DEFAULT_DURATION_DAYS);
            }
            case D_DAY -> {
                endAt = LocalDate.now();
                startAt = endAt.minusDays(DEFAULT_DURATION_DAYS);
            }
            case FINISHED -> {
                endAt = LocalDate.now().minusDays(1);
                startAt = endAt.minusDays(DEFAULT_DURATION_DAYS);
            }
            default -> {
                startAt = LocalDate.now();
                endAt = startAt.plusDays(DEFAULT_DURATION_DAYS);
            }
        }

        final ChallengeGroup challengeGroup = new ChallengeGroup(
                DEFAULT_NAME,
                DEFAULT_MAXIMUM_MEMBER_COUNT,
                startAt,
                endAt,
                JoinCode.generate(),
                LocalDateTime.now()
        );
        challengeGroup.updateStatus();
        return challengeGroup;
    }

    public static ChallengeGroup create(
            final String name,
            final int maximumMemberCount,
            final LocalDate startAt,
            final LocalDate endAt
    ) {
        return new ChallengeGroup(
                name,
                maximumMemberCount,
                startAt,
                endAt,
                JoinCode.generate(),
                LocalDateTime.now()
        );
    }
}
