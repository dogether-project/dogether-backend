package site.dogether.challengegroup.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;

class ChallengeGroupTest {

    @Test
    void 챌린지_그룹을_생성한다() {
        final String name = "매일 러닝 모임";
        final int maximumMemberCount = 10;
        final LocalDate startAt = LocalDate.now();
        final LocalDate endAt = startAt.plusDays(7);

        final ChallengeGroup created = ChallengeGroup.create(name, maximumMemberCount, startAt, endAt);

        assertSoftly(softly -> {
            assertThat(created.getId()).isNull();
            assertThat(created.getName()).isEqualTo(name);
            assertThat(created.getMaximumMemberCount()).isEqualTo(maximumMemberCount);
            assertThat(created.getStartAt()).isEqualTo(startAt);
            assertThat(created.getEndAt()).isEqualTo(endAt);
            assertThat(created.getJoinCode()).isNotNull();
            assertThat(created.getStatus()).isEqualTo(ChallengeGroupStatus.RUNNING);
        });
    }

    @Test
    void 챌린지_그룹을_불러온다() {
        final Long id = 1L;
        final String name = "매일 러닝 모임";
        final int maximumMemberCount = 10;
        final LocalDate startAt = LocalDate.now();
        final LocalDate endAt = startAt.plusDays(7);
        final String joinCode = "join_code";
        final ChallengeGroupStatus status = ChallengeGroupStatus.RUNNING;

        assertThatCode(() -> new ChallengeGroup(
                id,
                name,
                maximumMemberCount,
                startAt,
                endAt,
                joinCode,
                status
        )).doesNotThrowAnyException();
    }

    @NullAndEmptySource
    @ParameterizedTest
    void 챌린지_그룹명으로_null_또는_공백을_입력하면_예외가_발생한다(final String name) {
        final int maximumMemberCount = 10;
        final LocalDate startAt = LocalDate.now();
        final LocalDate endAt = startAt.plusDays(7);

        assertThatThrownBy(() -> ChallengeGroup.create(name, maximumMemberCount, startAt, endAt))
                .isInstanceOf(InvalidChallengeGroupException.class)
                .hasMessage(String.format("챌린지 그룹 이름으로 null 혹은 공백을 입력할 수 없습니다. (name : %s)", name));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 21})
    void 유효하지_않은_챌린지_그룹_최대_참여_인원을_입력하면_예외가_발생한다(final int maximumMemberCount) {
        final String name = "매일 러닝 모임";
        final LocalDate startAt = LocalDate.now();
        final LocalDate endAt = startAt.plusDays(7);

        assertThatThrownBy(() -> ChallengeGroup.create(name, maximumMemberCount, startAt, endAt))
                .isInstanceOf(InvalidChallengeGroupException.class)
                .hasMessage(String.format(
                        "챌린지 그룹 최대 인원은 2명 이상, 20명 이하만 가능합니다. (input : %d)", maximumMemberCount)
                );
    }

    @Test
    void 챌린지_그룹의_시작일이_종료일보다_늦으면_예외가_발생한다() {
        final String name = "매일 러닝 모임";
        final int maximumMemberCount = 10;
        final LocalDate startAt = LocalDate.now();
        final LocalDate endAt = startAt.minusDays(1);

        assertThatThrownBy(() -> ChallengeGroup.create(name, maximumMemberCount, startAt, endAt))
                .isInstanceOf(InvalidChallengeGroupException.class)
                .hasMessage(
                        String.format("시작일은 종료일보다 늦을 수 없습니다. (startAt : %s, endAt : %s)", startAt, endAt)
                );
    }

    @Test
    void 챌린지_그룹의_진행일을_계산한다__READY이면_진행일은_0이다() {
        final LocalDate startAt = LocalDate.now().plusDays(1);
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                1L,
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(7),
                "join_code",
                ChallengeGroupStatus.READY
        );

        final int progressDay = challengeGroup.getProgressDay();

        assertThat(progressDay).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6})
    void 챌린지_그룹의_진행일을_계산한다__RUNNING이면_하루씩_증가한다(final int daysSinceStart) {
        final LocalDate startAt = LocalDate.now().minusDays(daysSinceStart);
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                1L,
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(7),
                "join_code",
                ChallengeGroupStatus.RUNNING
        );

        final int progressDay = challengeGroup.getProgressDay();

        assertThat(progressDay).isEqualTo(daysSinceStart + 1);
    }

    @ParameterizedTest
    @ValueSource(ints = {7, 8, 9})
    void 챌린지_그룹의_진행일을_계산한다__FINISHED(final int daysSinceStart) {
        final LocalDate startAt = LocalDate.now().minusDays(daysSinceStart);
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                1L,
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(7),
                "join_code",
                ChallengeGroupStatus.FINISHED
        );

        final int progressDay = challengeGroup.getProgressDay();

        assertThat(progressDay).isEqualTo(7);
    }

    @Test
    void 챌린지_그룹의_진행률을_계산한다__READY이면_진행률은_0이다() {
        final LocalDate startAt = LocalDate.now().plusDays(1);
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                1L,
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(7),
                "join_code",
                ChallengeGroupStatus.READY
        );

        final double progressRate = challengeGroup.getProgressRate();

        assertThat(progressRate).isEqualTo(0);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0.14285714285714285",
            "1, 0.2857142857142857",
            "2, 0.42857142857142855",
            "3, 0.5714285714285714",
            "4, 0.7142857142857143",
            "5, 0.8571428571428571",
            "6, 1.0"
    })
    void 챌린지_그룹의_진행률을_계산한다__RUNNING이면_진행일_나누기_기간(final int daysSinceStart, final double expected) {
        final LocalDate startAt = LocalDate.now().minusDays(daysSinceStart);
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                1L,
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(7),
                "join_code",
                ChallengeGroupStatus.RUNNING
        );

        final double progressRate = challengeGroup.getProgressRate();

        assertThat(progressRate).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(ints = {7, 8, 9})
    void 챌린지_그룹의_진행률을_계산한다__FINISHED이면_1이다(final int daysSinceStart) {
        final LocalDate startAt = LocalDate.now().minusDays(daysSinceStart);
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                1L,
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(7),
                "join_code",
                ChallengeGroupStatus.FINISHED
        );

        final double progressRate = challengeGroup.getProgressRate();

        assertThat(progressRate).isEqualTo(1);
    }

    @Test
    void 챌린지_그룹의_상태를_갱신한다__READY에서_RUNNING으로() {
        final LocalDate startAt = LocalDate.now();
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                1L,
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(7),
                "join_code",
                ChallengeGroupStatus.READY
        );

        challengeGroup.updateStatus();

        assertThat(challengeGroup.getStatus()).isEqualTo(ChallengeGroupStatus.RUNNING);
    }

    @Test
    void 챌린지_그룹의_상태를_갱신한다__RUNNING에서_D_DAY로() {
        final LocalDate endAt = LocalDate.now();
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                1L,
                "매일 러닝 모임",
                10,
                endAt.minusDays(7),
                endAt,
                "join_code",
                ChallengeGroupStatus.RUNNING
        );

        challengeGroup.updateStatus();

        assertThat(challengeGroup.getStatus()).isEqualTo(ChallengeGroupStatus.D_DAY);
    }

    @Test
    void 챌린지_그룹의_상태를_갱신한다__D_DAY에서_FINISHED으로() {
        final LocalDate endAt = LocalDate.now().minusDays(1);
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                1L,
                "매일 러닝 모임",
                10,
                endAt.minusDays(7),
                endAt,
                "join_code",
                ChallengeGroupStatus.D_DAY
        );

        challengeGroup.updateStatus();

        assertThat(challengeGroup.getStatus()).isEqualTo(ChallengeGroupStatus.FINISHED);
    }
}
