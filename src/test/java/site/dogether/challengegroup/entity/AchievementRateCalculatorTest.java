package site.dogether.challengegroup.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationCount;
import site.dogether.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AchievementRateCalculatorTest {

    private static DailyTodo createDailyTodo(LocalDateTime writtenAt) {
        return new DailyTodo(
            1L,
            createChallengeGroup(),
            createMember(),
            "월레스와 인사하기",
            DailyTodoStatus.CERTIFY_PENDING,
            writtenAt
        );
    }

    private static ChallengeGroup createChallengeGroup() {
        return new ChallengeGroup(
            "그로밋의 일상생활",
            8,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            JoinCode.generate(),
            LocalDateTime.now().plusHours(1)
        );
    }

    private static Member createMember() {
        return new Member(1L, "provider_id", "테스터", "image_url", LocalDateTime.now());
    }

    private static DailyTodoCertificationCount createDailyTodoCertificationCount(int total, int approved) {
        return new DailyTodoCertificationCount((long) total, (long) approved, 0L);
    }

    @DisplayName("데일리 투두 리스트가 비어있으면 달성률은 0%이다.")
    @Test
    void returnZeroWhenTodosAreEmpty() {
        // Given
        List<DailyTodo> dailyTodos = List.of();
        LocalDate challengeGroupStartAt = LocalDate.of(2025, 6, 1);
        LocalDate challengeGroupEndAt = LocalDate.of(2025, 6, 10);
        DailyTodoCertificationCount count = createDailyTodoCertificationCount(0, 0);

        // When
        int result = AchievementRateCalculator.calculate(dailyTodos, challengeGroupStartAt, challengeGroupEndAt, count);

        // Then
        assertThat(result).isZero();
    }

    @DisplayName("그룹 종료일이 시작일보다 같거나 이전이면 달성률은 0%이다.")
    @Test
    void returnZeroWhenGroupDurationInvalid() {
        // Given
        List<DailyTodo> dailyTodos = List.of(
            createDailyTodo(LocalDateTime.of(2025, 6, 1, 10, 0))
        );
        LocalDate challengeGroupStartAt = LocalDate.of(2025, 6, 5);
        LocalDate challengeGroupEndAt = LocalDate.of(2025, 6, 5);
        DailyTodoCertificationCount count = createDailyTodoCertificationCount(1, 1);

        // When
        int result = AchievementRateCalculator.calculate(dailyTodos, challengeGroupStartAt, challengeGroupEndAt, count);

        // Then
        assertThat(result).isZero();
    }

    @DisplayName("작성한 투두, 인증, 인정, 참여일이 모두 최대일 때 달성률은 100%이다.")
    @Test
    void returnHundredWhenAllConditionsAreMax() {
        // Given
        LocalDate challengeGroupStartAt = LocalDate.of(2025, 6, 1);
        LocalDate challengeGroupEndAt = LocalDate.of(2025, 6, 2);
        final int groupTotalTodoLimit = 10;

        List<DailyTodo> dailyTodos = List.of(
            createDailyTodo(LocalDateTime.of(2025, 6, 1, 8, 0)),
            createDailyTodo(LocalDateTime.of(2025, 6, 1, 9, 0)),
            createDailyTodo(LocalDateTime.of(2025, 6, 1, 10, 0)),
            createDailyTodo(LocalDateTime.of(2025, 6, 1, 11, 0)),
            createDailyTodo(LocalDateTime.of(2025, 6, 1, 12, 0)),
            createDailyTodo(LocalDateTime.of(2025, 6, 1, 1, 0)),
            createDailyTodo(LocalDateTime.of(2025, 6, 1, 2, 0)),
            createDailyTodo(LocalDateTime.of(2025, 6, 1, 3, 0)),
            createDailyTodo(LocalDateTime.of(2025, 6, 1, 4, 0)),
            createDailyTodo(LocalDateTime.of(2025, 6, 1, 5, 0))
        ).subList(0, groupTotalTodoLimit);

        DailyTodoCertificationCount count = createDailyTodoCertificationCount(groupTotalTodoLimit, groupTotalTodoLimit);

        // When
        int result = AchievementRateCalculator.calculate(dailyTodos, challengeGroupStartAt, challengeGroupEndAt, count);

        // Then
        assertThat(result).isEqualTo(100);
    }

    @DisplayName("챌린지 그룹에 참여는 했지만 데일리 투두 작성이 없으면 달성률은 0%이다.")
    @Test
    void returnPartialWhenOnlyParticipated() {
        // Given
        LocalDate challengeGroupStartAt = LocalDate.of(2025, 6, 1);
        LocalDate challengeGroupEndAt = LocalDate.of(2025, 6, 6);
        List<DailyTodo> dailyTodos = List.of();
        DailyTodoCertificationCount count = createDailyTodoCertificationCount(0, 0);

        // When
        int result = AchievementRateCalculator.calculate(dailyTodos, challengeGroupStartAt, challengeGroupEndAt, count);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @DisplayName("데일리 투두 작성은 했지만 인증 및 인정이 없을 경우 관련 점수는 0이 되어 달성률이 높아지지 않는다.")
    @Test
    void partialScoreWhenNoCertifyOrApprove() {
        // Given
        LocalDate challengeGroupStartAt = LocalDate.of(2025, 6, 1);
        LocalDate challengeGroupEndAt = LocalDate.of(2025, 6, 6);

        List<DailyTodo> dailyTodos = List.of(
            createDailyTodo(LocalDateTime.of(2025, 6, 1, 8, 0)),
            createDailyTodo(LocalDateTime.of(2025, 6, 2, 9, 0)),
            createDailyTodo(LocalDateTime.of(2025, 6, 3, 10, 0))
        );

        DailyTodoCertificationCount count = createDailyTodoCertificationCount(0, 0);

        // When
        int result = AchievementRateCalculator.calculate(dailyTodos, challengeGroupStartAt, challengeGroupEndAt, count);

        // Then
        assertThat(result).isLessThan(100).isGreaterThan(0);
    }
}