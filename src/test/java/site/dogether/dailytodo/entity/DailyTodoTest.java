package site.dogether.dailytodo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.dailytodo.exception.InvalidDailyTodoException;
import site.dogether.member.entity.Member;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static site.dogether.dailytodo.entity.DailyTodo.*;
import static site.dogether.dailytodo.entity.DailyTodoStatus.*;

class DailyTodoTest {

    private static ChallengeGroup createChallengeGroup() {
        return new ChallengeGroup(
            1L,
            "성욱이와 친구들",
            8,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            "join_code",
            ChallengeGroupStatus.RUNNING);
    }

    private static Member createMember() {
        return new Member(
            1L,
            "provider_id",
            "성욱쨩",
            "profile_image_url"
        );
    }

    @DisplayName("생성자에 유효한 입력값(id, 챌린지 그룹, 멤버, 투두 내용, 투두 상태, 노인정 사유)을 입력하면 DailyTodo 인스턴스가 생성된다.")
    @Test
    void createDailyTodo() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member member = createMember();
        final String content = "치킨 먹기";
        final DailyTodoStatus status = REJECT;
        final String rejectReason = "그딴게 치킨?";

        // When && Then
        assertThatCode(() -> new DailyTodo(
            1L,
            challengeGroup,
            member,
            content,
            status,
            rejectReason))
            .doesNotThrowAnyException();
    }

    @DisplayName("생성자에 유효한 일부 입력값(챌린지 그룹, 멤버, 투두 내용)을 입력하면 기본값과 함께 DailyTodo 인스턴스가 생성된다.")
    @Test
    void createDailyTodoWithDefaultValue() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member member = createMember();
        final String content = "치킨 먹기";

        // When
        final DailyTodo created = new DailyTodo(challengeGroup, member, content);

        // Then
        assertSoftly(softly -> {
            assertThat(created.getId()).isNull();
            assertThat(created.getStatus()).isEqualTo(CERTIFY_PENDING);
            assertThat(created.getRejectReason()).isEmpty();
        });
    }

    @Test
    @DisplayName("생성자에 챌린지 그룹으로 null을 입력하면 예외가 발생한다.")
    void throwExceptionWhenInputChallengeGroupNull() {
        // Given
        final Member member = createMember();
        final String content = "치킨 먹기";

        // When & Then
        assertThatThrownBy(() -> new DailyTodo(null, member, content))
            .isInstanceOf(InvalidDailyTodoException.class)
            .hasMessage("데일리 투두 챌린지 그룹으로 null을 입력할 수 없습니다.");
    }

    @Test
    @DisplayName("생성자에 작성자로 null을 입력하면 예외가 발생한다.")
    void throwExceptionWhenInputMemberNull() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final String content = "치킨 먹기";

        // When & Then
        assertThatThrownBy(() -> new DailyTodo(challengeGroup, null, content))
            .isInstanceOf(InvalidDailyTodoException.class)
            .hasMessage("데일리 투두 작성자로 null을 입력할 수 없습니다.");
    }

    @DisplayName("생성자에 투두 내용으로 null 혹은 공백을 입력하면 예외가 발생한다.")
    @NullAndEmptySource
    @ParameterizedTest()
    void throwExceptionWhenInputContentNullOrEmpty(final String content) {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member member = createMember();

        // When & Then
        assertThatThrownBy(() -> new DailyTodo(challengeGroup, member, content))
            .isInstanceOf(InvalidDailyTodoException.class)
            .hasMessage(String.format("데일리 투두 내용으로 null 혹은 공백을 입력할 수 없습니다. (%s)", content));
    }

    @DisplayName("생성자에 유효하지 않은 길이의 투두 내용을 입력하면 예외가 발생한다.")
    @Test()
    void throwExceptionWhenInputInvalidLengthContent() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member member = createMember();
        final String content = "A".repeat(MAXIMUM_ALLOWED_CONTENT_LENGTH + 1);

        // When & Then
        assertThatThrownBy(() -> new DailyTodo(challengeGroup, member, content))
            .isInstanceOf(InvalidDailyTodoException.class)
            .hasMessage(String.format("데일리 투두 내용은 %d자 이하만 입력할 수 있습니다. (%d)", MAXIMUM_ALLOWED_CONTENT_LENGTH, content.length()));
    }

    @DisplayName("생성자에 데일리 투두 상태로 null을 입력하면 예외가 발생한다.")
    @Test()
    void throwExceptionWhenInputStatusNull() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member member = createMember();
        final String content = "치킨 먹기";
        final String rejectReason = "그딴게 치킨?";

        // When & Then
        assertThatThrownBy(() -> new DailyTodo(1L, challengeGroup, member, content, null, rejectReason))
            .isInstanceOf(InvalidDailyTodoException.class)
            .hasMessage("데일리 투두 상태로 null을 입력할 수 없습니다.");
    }

    @DisplayName("데일리 투두가 노인정 상태가 아닐 때 노인정 사유가 입력되면 예외가 발생한다.")
    @Test()
    void throwExceptionWhenStatusNotRejectAndInputRejectReason() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member member = createMember();
        final String content = "치킨 먹기";
        final DailyTodoStatus status = APPROVE;
        final String rejectReason = "이딴게 치킨?";

        // When & Then
        assertThatThrownBy(() -> new DailyTodo(1L, challengeGroup, member, content, status, rejectReason))
            .isInstanceOf(InvalidDailyTodoException.class)
            .hasMessage(String.format("데일리 투두가 노인정 상태가 아니면 노인정 사유를 입력할 수 없습니다. (%s)", rejectReason));
    }

    @DisplayName("데일리 투두가 노인정 상태일 때 노인정 사유로 null 혹은 공백이 입력되면 예외가 발생한다.")
    @NullAndEmptySource
    @ParameterizedTest()
    void throwExceptionWhenStatusRejectAndInputRejectReasonNullOrEmpty(final String rejectReason) {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member member = createMember();
        final String content = "치킨 먹기";

        // When & Then
        assertThatThrownBy(() -> new DailyTodo(1L, challengeGroup, member, content, REJECT, rejectReason))
            .isInstanceOf(InvalidDailyTodoException.class)
            .hasMessage(String.format("노인정 사유로 null 혹은 공백을 입력할 수 없습니다. (%s)", rejectReason));
    }

    @DisplayName("데일리 투두가 노인정 상태일 때 유효하지 않은 길이의 노인정 사유가 입력되면 예외가 발생한다.")
    @Test()
    void throwExceptionWhenStatusRejectAndInputInvalidLengthRejectReason() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member member = createMember();
        final String content = "치킨 먹기";
        final String rejectReason = "A".repeat(MAXIMUM_ALLOWED_REJECT_REASON_LENGTH + 1);

        // When & Then
        assertThatThrownBy(() -> new DailyTodo(1L, challengeGroup, member, content, REJECT, rejectReason))
            .isInstanceOf(InvalidDailyTodoException.class)
            .hasMessage(String.format("노인정 사유는 %d자 이하만 입력할 수 있습니다. (%d)", MAXIMUM_ALLOWED_REJECT_REASON_LENGTH, rejectReason.length()));
    }
}
