package site.dogether.challengegroup.domain;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;

@RequiredArgsConstructor
public enum ChallengeGroupDurationOption {

    THREE_DAYS(3),
    SEVEN_DAYS(7),
    FOURTEEN_DAYS(14),
    TWENTY_EIGHT_DAYS(28)
    ;

    private final int value;

    public static ChallengeGroupDurationOption from(final int durationOption) {
        return Arrays.stream(ChallengeGroupDurationOption.values())
                .filter(option -> option.value == durationOption)
                .findAny()
                .orElseThrow(() -> new InvalidChallengeGroupException("유효하지 않은 기간 옵션입니다. " + durationOption + "일"));
    }
}
