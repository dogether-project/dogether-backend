package site.dogether.challengegroup.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class ChallengeGroupTest {

    @Test
    void 챌린지_그룹을_생성한다() {
        final String name = "매일 러닝 모임";
        final int maximumMemberCount = 10;
        final LocalDate startAt = LocalDate.now();
        final LocalDate endAt = startAt.plusDays(7);
        final JoinCode joinCode = JoinCode.generate();
        final LocalDateTime createdAt = LocalDateTime.now();

        final ChallengeGroup created = new ChallengeGroup(name, maximumMemberCount, startAt, endAt, joinCode, createdAt);

        assertSoftly(softly -> {
            assertThat(created.getId()).isNull();
            assertThat(created.getName()).isEqualTo(name);
            assertThat(created.getMaximumMemberCount()).isEqualTo(maximumMemberCount);
            assertThat(created.getStartAt()).isEqualTo(startAt);
            assertThat(created.getEndAt()).isEqualTo(endAt);
            assertThat(created.getJoinCode()).isNotNull();
            assertThat(created.getStatus()).isEqualTo(ChallengeGroupStatus.RUNNING);
            assertThat(created.getCreatedAt()).isEqualTo(createdAt);
        });
    }


    @NullAndEmptySource
    @ParameterizedTest
    void 챌린지_그룹명으로_null_또는_공백을_입력하면_예외가_발생한다(final String name) {
        final int maximumMemberCount = 10;
        final LocalDate startAt = LocalDate.now();
        final LocalDate endAt = startAt.plusDays(7);
        final JoinCode joinCode = JoinCode.generate();
        final LocalDateTime createdAt = LocalDateTime.now();

        assertThatThrownBy(() -> new ChallengeGroup(name, maximumMemberCount, startAt, endAt, joinCode, createdAt))
                .isInstanceOf(InvalidChallengeGroupException.class)
                .hasMessage(String.format("챌린지 그룹 이름으로 null 혹은 공백을 입력할 수 없습니다. (name : %s)", name));
    }

    @Test
    void 챌린지_그룹_이름이_200자를_초과하면_예외가_발생한다() {
        final String name = "a".repeat(201);
        final int maximumMemberCount = 10;
        final LocalDate startAt = LocalDate.now();
        final LocalDate endAt = startAt.plusDays(7);
        final JoinCode joinCode = JoinCode.generate();
        final LocalDateTime createdAt = LocalDateTime.now();

        assertThatThrownBy(() -> new ChallengeGroup(name, maximumMemberCount, startAt, endAt, joinCode, createdAt))
                .isInstanceOf(InvalidChallengeGroupException.class)
                .hasMessage(String.format("챌린지 그룹 이름은 1자 이상, 200자 이하만 가능합니다. (name : %s)", name));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 21})
    void 유효하지_않은_챌린지_그룹_최대_참여_인원을_입력하면_예외가_발생한다(final int maximumMemberCount) {
        final String name = "매일 러닝 모임";
        final LocalDate startAt = LocalDate.now();
        final LocalDate endAt = startAt.plusDays(7);
        final JoinCode joinCode = JoinCode.generate();
        final LocalDateTime createdAt = LocalDateTime.now();

        assertThatThrownBy(() -> new ChallengeGroup(name, maximumMemberCount, startAt, endAt, joinCode, createdAt))
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
        final JoinCode joinCode = JoinCode.generate();
        final LocalDateTime createdAt = LocalDateTime.now();

        assertThatThrownBy(() -> new ChallengeGroup(name, maximumMemberCount, startAt, endAt, joinCode, createdAt))
                .isInstanceOf(InvalidChallengeGroupException.class)
                .hasMessage(
                        String.format("시작일은 종료일보다 늦을 수 없습니다. (startAt : %s, endAt : %s)", startAt, endAt)
                );
    }

    @Test
    void 챌린지_그룹의_진행일을_계산한다__시작_전이면_진행일은_0이다() {
        final LocalDate startAt = LocalDate.now().plusDays(1);
        final LocalDateTime createdAt = LocalDateTime.now();
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(7),
                JoinCode.generate(),
                createdAt
        );

        final int progressDay = challengeGroup.getProgressDay();

        assertThat(progressDay).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    void 챌린지_그룹의_진행일을_계산한다__진행중이면_하루씩_증가한다(final int daysSinceStart) {
        final LocalDate startAt = LocalDate.now().minusDays(daysSinceStart);
        final LocalDateTime createdAt = LocalDateTime.now();
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(7),
                JoinCode.generate(),
                createdAt
        );

        final int progressDay = challengeGroup.getProgressDay();

        assertThat(progressDay).isEqualTo(daysSinceStart + 1);
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 9})
    void 챌린지_그룹의_진행일을_계산한다__종료_후엔_최대값_유지(final int daysSinceStart) {
        final LocalDate startAt = LocalDate.now().minusDays(daysSinceStart);
        int duration = 7;
        final LocalDateTime createdAt = LocalDateTime.now();
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(duration),
                JoinCode.generate(),
                createdAt
        );

        final int progressDay = challengeGroup.getProgressDay();

        assertThat(progressDay).isEqualTo(duration + 1);
    }

    @Test
    void 챌린지_그룹의_진행률을_계산한다__시작_전이면_진행률은_0이다() {
        final LocalDate startAt = LocalDate.now().plusDays(1);
        final LocalDateTime createdAt = LocalDateTime.now();
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(7),
                JoinCode.generate(),
                createdAt
        );

        final double progressRate = challengeGroup.getProgressRate();

        assertThat(progressRate).isEqualTo(0);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0.14",
            "1, 0.29",
            "2, 0.43",
            "3, 0.57",
            "4, 0.71",
            "5, 0.86",
            "6, 1.0",
            "7, 1.0"
    })
    void 챌린지_그룹의_진행률을_계산한다__진행중이면_날짜에_따라_증가(final int daysSinceStart, final double expected) {
        final LocalDate startAt = LocalDate.now().minusDays(daysSinceStart);
        final LocalDateTime createdAt = LocalDateTime.now();
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(7),
                JoinCode.generate(),
                createdAt
        );

        final double progressRate = challengeGroup.getProgressRate();

        assertThat(progressRate).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 9})
    void 챌린지_그룹의_진행률을_계산한다__종료_후엔_1이다(final int daysSinceStart) {
        final LocalDate startAt = LocalDate.now().minusDays(daysSinceStart);
        final LocalDateTime createdAt = LocalDateTime.now();
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(7),
                JoinCode.generate(),
                createdAt
        );

        final double progressRate = challengeGroup.getProgressRate();

        assertThat(progressRate).isEqualTo(1);
    }

    @Test
    void 챌린지_그룹의_상태를_갱신한다__시작_전이면_READY() {
        final LocalDate startAt = LocalDate.now().plusDays(1);
        final LocalDateTime createdAt = LocalDateTime.now();
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(7),
                JoinCode.generate(),
                createdAt
        );

        challengeGroup.updateStatus();

        assertThat(challengeGroup.getStatus()).isEqualTo(ChallengeGroupStatus.READY);
    }

    @Test
    void 챌린지_그룹의_상태를_갱신한다__진행_중이면_RUNNING() {
        final LocalDate startAt = LocalDate.now().minusDays(3);
        final LocalDateTime createdAt = LocalDateTime.now();
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                "매일 러닝 모임",
                10,
                startAt,
                startAt.plusDays(7),
                JoinCode.generate(),
                createdAt
        );

        challengeGroup.updateStatus();

        assertThat(challengeGroup.getStatus()).isEqualTo(ChallengeGroupStatus.RUNNING);
    }

    @Test
    void 챌린지_그룹의_상태를_갱신한다__마지막_날이면_D_DAY() {
        final LocalDate endAt = LocalDate.now();
        final LocalDateTime createdAt = LocalDateTime.now();
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                "매일 러닝 모임",
                10,
                endAt.minusDays(7),
                endAt,
                JoinCode.generate(),
                createdAt
        );

        challengeGroup.updateStatus();

        assertThat(challengeGroup.getStatus()).isEqualTo(ChallengeGroupStatus.D_DAY);
    }

    @Test
    void 챌린지_그룹의_상태를_갱신한다__종료_후면_FINISHED() {
        final LocalDate endAt = LocalDate.now().minusDays(1);
        final LocalDateTime createdAt = LocalDateTime.now();
        final ChallengeGroup challengeGroup = new ChallengeGroup(
                "매일 러닝 모임",
                10,
                endAt.minusDays(7),
                endAt,
                JoinCode.generate(),
                createdAt
        );

        challengeGroup.updateStatus();

        assertThat(challengeGroup.getStatus()).isEqualTo(ChallengeGroupStatus.FINISHED);
    }
}
