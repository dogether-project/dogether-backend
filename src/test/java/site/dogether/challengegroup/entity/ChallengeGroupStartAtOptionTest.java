package site.dogether.challengegroup.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import site.dogether.challengegroup.exception.InvalidChallengeGroupStartAtException;

class ChallengeGroupStartAtOptionTest {

    @Test
    void 챌린지_그룹_시작_옵션을_생성한다__TODAY() {
        final String startAtOptionValue = "TODAY";

        assertThat(ChallengeGroupStartAtOption.from(startAtOptionValue)).isEqualTo(ChallengeGroupStartAtOption.TODAY);
    }

    @Test
    void 챌린지_그룹_시작_옵션을_생성한다__TOMORROW() {
        final String startAtOptionValue = "TOMORROW";

        assertThat(ChallengeGroupStartAtOption.from(startAtOptionValue)).isEqualTo(ChallengeGroupStartAtOption.TOMORROW);
    }

    @NullAndEmptySource
    @ParameterizedTest
    void 챌린지_그룹_시작_옵션으로_null이나_빈값을_입력하면_예외를_던진다(String startAtOptionValue) {
        assertThatThrownBy(() -> ChallengeGroupStartAtOption.from(startAtOptionValue))
                .isInstanceOf(InvalidChallengeGroupStartAtException.class)
                .hasMessage("시작일은 필수 입력값입니다.");
    }

    @Test
    void 유효하지_않은_챌린지_그룹_시작_옵션을_입력하면_예외를_던진다() {
        final String startAtOptionValue = "YESTERDAY";

        assertThatThrownBy(() -> ChallengeGroupStartAtOption.from(startAtOptionValue))
                .isInstanceOf(InvalidChallengeGroupStartAtException.class)
                .hasMessage("유효하지 않은 시작일 옵션입니다.");
    }

    @Test
    void 옵션에_따라_시작일을_계산한다_TODAY() {
        ChallengeGroupStartAtOption startAtOption = ChallengeGroupStartAtOption.TODAY;

        assertThat(startAtOption.calculateStartAt()).isEqualTo(LocalDate.now());
    }

    @Test
    void 옵션에_따라_시작일을_계산한다_TOMORROW() {
        ChallengeGroupStartAtOption startAtOption = ChallengeGroupStartAtOption.TOMORROW;

        assertThat(startAtOption.calculateStartAt()).isEqualTo(LocalDate.now().plusDays(1));
    }
}
