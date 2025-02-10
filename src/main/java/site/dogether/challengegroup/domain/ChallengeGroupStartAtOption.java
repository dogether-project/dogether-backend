package site.dogether.challengegroup.domain;

import java.time.LocalDateTime;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;

public enum ChallengeGroupStartAtOption {

    TODAY,
    TOMORROW
    ;

    public static ChallengeGroupStartAtOption from(final String startAt) {
        validateStartAt(startAt);
        return ChallengeGroupStartAtOption.valueOf(startAt.toUpperCase());
    }

    private static void validateStartAt(final String startAt) {
        if (startAt == null || startAt.isBlank()) {
            throw new InvalidChallengeGroupException("시작일은 필수 입력값입니다.");
        }
        if (!startAt.equals("TODAY") && !startAt.equals("TOMORROW")) {
            throw new InvalidChallengeGroupException("유효하지 않은 시작일 옵션입니다.");
        }
    }

    public static ChallengeGroupStartAtOption from(final LocalDateTime startAt, final LocalDateTime createdAt) {
        if (startAt.getDayOfYear() == createdAt.getDayOfYear()) {
            return TODAY;
        }
        return TOMORROW;
    }
}
