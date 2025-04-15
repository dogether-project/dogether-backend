package site.dogether.challengegroup.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum ChallengeGroupDurationOption {

    THREE_DAYS(3, startAt -> startAt.plusDays(3)),
    SEVEN_DAYS(7, startAt -> startAt.plusDays(7)),
    FOURTEEN_DAYS(14, startAt -> startAt.plusDays(14)),
    TWENTY_EIGHT_DAYS(28, startAt -> startAt.plusDays(28)),
    ;

    private final int value;
    private final Function<LocalDate, LocalDate> endAtCalculator;

    public static ChallengeGroupDurationOption from(final int durationOption) {
        return Arrays.stream(ChallengeGroupDurationOption.values())
            .filter(option -> option.value == durationOption)
            .findAny()
            .orElseThrow(() -> new InvalidChallengeGroupException("유효하지 않은 기간 옵션입니다. " + durationOption + "일"));
    }

    public LocalDate calculateEndAt(final LocalDate startAt) {
        return endAtCalculator.apply(startAt);
    }
}
