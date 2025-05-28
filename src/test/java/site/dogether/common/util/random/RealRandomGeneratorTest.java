package site.dogether.common.util.random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RealRandomGeneratorTest {

    @DisplayName("시작 보다 작은 끝 숫자를 입력하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenInputCurrentNumber() {
        // Given
        final RealRandomGenerator randomGenerator = new RealRandomGenerator();

        final int start = 5;
        final int end = 4;

        // When & Then
        assertThatThrownBy(() -> randomGenerator.generateNumberInRange(start, end))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(String.format("start는 end보다 작거나 같아야 합니다. (start : %d) (end : %d)", start, end));
    }
}
