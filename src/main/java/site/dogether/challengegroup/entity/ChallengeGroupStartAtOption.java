package site.dogether.challengegroup.entity;

import lombok.RequiredArgsConstructor;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;

import java.time.LocalDateTime;
import java.util.function.Supplier;

@RequiredArgsConstructor
public enum ChallengeGroupStartAtOption {

    TODAY(LocalDateTime::now),
    TOMORROW(() -> LocalDateTime.now().plusDays(1)),
    ;

    private final Supplier<LocalDateTime> startAtCalculator;

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

    public LocalDateTime calculateStartAt() {
        return startAtCalculator.get();
    }
}
