package site.dogether.challengegroup.entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ChallengeGroupDurationOptionTest {

    @ParameterizedTest(name = "{0}일 - {1}")
    @MethodSource(value = "durationOptionSource")
    void 챌린지_그룹_기간_옵션을_생성한다(int durationOptionValue, ChallengeGroupDurationOption expected) {
        assertThat(ChallengeGroupDurationOption.from(durationOptionValue)).isEqualTo(expected);
    }

    static Stream<Arguments> durationOptionSource() {
        return Stream.of(
            Arguments.of(3, ChallengeGroupDurationOption.THREE_DAYS),
            Arguments.of(7, ChallengeGroupDurationOption.SEVEN_DAYS),
            Arguments.of(14, ChallengeGroupDurationOption.FOURTEEN_DAYS),
            Arguments.of(28, ChallengeGroupDurationOption.TWENTY_EIGHT_DAYS)
        );
    }

    @ParameterizedTest(name = "{0} - 종료일 {1}")
    @MethodSource(value = "calculateEndAtSource")
    void 종료일을_계산한다(ChallengeGroupDurationOption option, LocalDate expected) {
        final LocalDate startAt = LocalDate.now();

        LocalDate endAt = option.calculateEndAt(startAt);

        assertThat(endAt).isEqualTo(expected);
    }

    static Stream<Arguments> calculateEndAtSource() {
        return Stream.of(
            Arguments.of(ChallengeGroupDurationOption.THREE_DAYS, LocalDate.now().plusDays(3)),
            Arguments.of(ChallengeGroupDurationOption.SEVEN_DAYS, LocalDate.now().plusDays(7)),
            Arguments.of(ChallengeGroupDurationOption.FOURTEEN_DAYS, LocalDate.now().plusDays(14)),
            Arguments.of(ChallengeGroupDurationOption.TWENTY_EIGHT_DAYS, LocalDate.now().plusDays(28))
        );
    }
}
