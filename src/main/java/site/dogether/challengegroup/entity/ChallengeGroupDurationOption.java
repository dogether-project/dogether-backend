package site.dogether.challengegroup.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;

import java.time.LocalDateTime;
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

    public static ChallengeGroupDurationOption from(final LocalDateTime startAt, final LocalDateTime endAt) {
        final int duration = endAt.getDayOfYear() - startAt.getDayOfYear();
        return from(duration);
    }

    public static ChallengeGroupDurationOption from(final int durationOption) {
        return Arrays.stream(ChallengeGroupDurationOption.values())
            .filter(option -> option.value == durationOption)
            .findAny()
            .orElseThrow(() -> new InvalidChallengeGroupException("유효하지 않은 기간 옵션입니다. " + durationOption + "일"));
    }

    private final int value;
    private final Function<LocalDateTime, LocalDateTime> endAtCalculator;

    public LocalDateTime calculateEndAt(final LocalDateTime startAt) {
        return endAtCalculator.apply(startAt);
    }
}
