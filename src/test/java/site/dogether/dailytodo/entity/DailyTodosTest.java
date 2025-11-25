package site.dogether.dailytodo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.fixture.ChallengeGroupFixture;
import site.dogether.dailytodo.exception.InvalidDailyTodoException;
import site.dogether.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static site.dogether.dailytodo.entity.DailyTodoStatus.CERTIFY_PENDING;
import static site.dogether.dailytodo.entity.DailyTodos.MAXIMUM_ALLOWED_VALUE_COUNT;

class DailyTodosTest {

    private static Member createMember() {
        return new Member(
            1L,
            "provider_id",
            "성욱쨩",
            "profile_image_url",
            LocalDateTime.now()
        );
    }

    @DisplayName("생성자에 유효한 데일리 투두 리스트를 입력하면 DailyTodos 인스턴스가 생성된다.")
    @Test
    void createDailyTodos() {
        // Given
        final ChallengeGroup challengeGroup = ChallengeGroupFixture.create("성욱이와 친구들");
        final Member member = createMember();
        final LocalDateTime writtenAt = LocalDateTime.now();
        final List<DailyTodo> input = List.of(
            new DailyTodo(1L, challengeGroup, member, "치킨 먹기", CERTIFY_PENDING, writtenAt),
            new DailyTodo(2L, challengeGroup, member, "코딩 하기", CERTIFY_PENDING, writtenAt),
            new DailyTodo(3L, challengeGroup, member, "운동 하기", CERTIFY_PENDING, writtenAt)
        );

        // When & Then
        assertThatCode(() -> new DailyTodos(input))
            .doesNotThrowAnyException();
    }

    @DisplayName("생성자에 null 혹은 빈 리스트를 입력하면 예외가 발생한다.")
    @NullAndEmptySource
    @ParameterizedTest
    void throwExceptionWhenInputNullOrEmptyList(final List<DailyTodo> input) {
        // When & Then
        assertThatThrownBy(() -> new DailyTodos(input))
            .isInstanceOf(InvalidDailyTodoException.class)
            .hasMessage(String.format("데일리 투두로 null 혹은 빈 리스트를 입력할 수 없습니다. (%s)", input));
    }

    @Test
    @DisplayName("생성자에 유효하지 않은 길이의 리스트를 입력하면 예외가 발생한다.")
    void throwExceptionWhenInputInvalidSizeList() {
        // Given
        final ChallengeGroup challengeGroup = ChallengeGroupFixture.create("성욱이와 친구들");
        final Member member = createMember();
        final LocalDateTime writtenAt = LocalDateTime.now();
        final List<DailyTodo> input = IntStream.rangeClosed(1, 11)
            .mapToObj(i -> new DailyTodo((long) i, challengeGroup, member, "치킨 먹기", CERTIFY_PENDING, writtenAt))
            .collect(Collectors.toList());

        // When & Then
        assertThatThrownBy(() -> new DailyTodos(input))
            .isInstanceOf(InvalidDailyTodoException.class)
            .hasMessage(String.format("데일리 투두는 %d개 이하만 입력할 수 있습니다. (%d) (%s)", MAXIMUM_ALLOWED_VALUE_COUNT, input.size(), input));
    }
}
