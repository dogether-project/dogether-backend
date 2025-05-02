package site.dogether.dailytodo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import site.dogether.dailytodo.exception.InvalidDailyTodoStatusException;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static site.dogether.dailytodo.entity.DailyTodoStatus.*;

class DailyTodoStatusTest {

    @DisplayName("유효한 투두 상태 문자열이 입력되면 일치하는 DailyTodoStatus enum 값을 반환한다.")
    @MethodSource("stringAndEnumValues")
    @ParameterizedTest
    void convertFromValueSuccess(final String stringValue, final DailyTodoStatus enumValue) {
        // When
        final DailyTodoStatus result = convertFromValue(stringValue);

        // Then
        assertThat(result).isEqualTo(enumValue);
    }

    private static Stream<Arguments> stringAndEnumValues() {
        return Stream.of(
            Arguments.of("CERTIFY_PENDING", CERTIFY_PENDING),
            Arguments.of("certify_pending", CERTIFY_PENDING),
            Arguments.of("REVIEW_PENDING", REVIEW_PENDING),
            Arguments.of("review_pending", REVIEW_PENDING),
            Arguments.of("APPROVE", APPROVE),
            Arguments.of("approve", APPROVE),
            Arguments.of("REJECT", REJECT),
            Arguments.of("reject", REJECT)
        );
    }

    @DisplayName("null, 공백 혹은 유효하지 않은 문자열이 입력되면 예외가 발생한다.")
    @NullAndEmptySource
    @ValueSource(strings = {"invalid", "done", "APPROVED", "123"})
    @ParameterizedTest
    void throwExceptionWhenInputInvalidStringValue(final String stringValue) {
        // When & Then
        assertThatThrownBy(() -> DailyTodoStatus.convertFromValue(stringValue))
            .isInstanceOf(InvalidDailyTodoStatusException.class)
            .hasMessage(String.format("유효하지 않은 데일리 투두 상태 값입니다. (%s)", stringValue));
    }
}
