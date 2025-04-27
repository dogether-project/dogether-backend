package site.dogether.challengegroup.entity;

import java.time.LocalDate;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import site.dogether.challengegroup.exception.InvalidChallengeGroupStartAtException;

@RequiredArgsConstructor
public enum ChallengeGroupStartAtOption {

    TODAY(LocalDate::now),
    TOMORROW(() -> LocalDate.now().plusDays(1)),
    ;

    private final Supplier<LocalDate> startAtCalculator;

    public static ChallengeGroupStartAtOption from(final String startAt) {
        validateStartAt(startAt);
        return ChallengeGroupStartAtOption.valueOf(startAt.toUpperCase());
    }

    private static void validateStartAt(final String startAt) {
        if (startAt == null || startAt.isBlank()) {
            throw new InvalidChallengeGroupStartAtException("시작일은 필수 입력값입니다.");
        }

        if (!startAt.equals("TODAY") && !startAt.equals("TOMORROW")) {
            throw new InvalidChallengeGroupStartAtException("유효하지 않은 시작일 옵션입니다.");
        }
    }

    public LocalDate calculateStartAt() {
        return startAtCalculator.get();
    }
}
