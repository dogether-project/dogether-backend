package site.dogether.dailytodocertification.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.fixture.ChallengeGroupFixture;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodocertification.exception.InvalidDailyTodoCertificationException;
import site.dogether.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static site.dogether.dailytodocertification.entity.DailyTodoCertification.MAXIMUM_ALLOWED_CONTENT_LENGTH;

class DailyTodoCertificationTest {

    private static Member createMember(final Long id, final String name) {
        return new Member(
            id,
            "provider_id",
            name,
            "profile_image_url",
            LocalDateTime.now()
        );
    }

    private static DailyTodo createDailyTodo(
        final ChallengeGroup challengeGroup,
        final Member member,
        final DailyTodoStatus status,
        final LocalDateTime writtenAt
    ) {
        return new DailyTodo(
            1L,
            challengeGroup,
            member,
            "치킨 먹기",
            status,
            writtenAt
        );
    }

    @DisplayName("생성자에 유효한 입력값을 넘기면 DailyTodoCertification 인스턴스가 생성된다.")
    @Test
    void createSuccess() {
        // Given
        final DailyTodo dailyTodo = createDailyTodo(
            ChallengeGroupFixture.create("성욱이와 친구들"),
            createMember(1L, "투두 작성자"),
            DailyTodoStatus.CERTIFY_PENDING,
            LocalDateTime.now()
        );
        final String certifyContent = "치킨 냠냠 인증!";
        final String certifyMediaUrl = "https://냠냠.png";

        // When & Then
        assertThatCode(() -> new DailyTodoCertification(
            dailyTodo,
            certifyContent,
            certifyMediaUrl
        ))
        .doesNotThrowAnyException();
    }

    @DisplayName("생성자에 데일리 투두로 null을 입력하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenInputDailyTodoNull() {
        // Given
        final String certifyContent = "치킨 냠냠 인증!";
        final String certifyMediaUrl = "https://냠냠.png";

        // When & Then
        assertThatThrownBy(() -> new DailyTodoCertification(
            null,
            certifyContent,
            certifyMediaUrl
        ))
            .isInstanceOf(InvalidDailyTodoCertificationException.class)
            .hasMessage("데일리 투두 인증 생성에 데일리 투두로 null을 입력할 수 없습니다.");
    }



    @DisplayName("생성자에 데일리 투두 인증 본문으로 null 혹은 공백을 입력하면 예외가 발생한다.")
    @NullAndEmptySource
    @ParameterizedTest
    void throwExceptionWhenInputContentNullOrEmpty(final String certifyContent) {
        // Given
        final DailyTodo dailyTodo = createDailyTodo(
            ChallengeGroupFixture.create("성욱이와 친구들"),
            createMember(2L, "투두 검사자"),
            DailyTodoStatus.CERTIFY_PENDING,
            LocalDateTime.now()
        );
        final String certifyMediaUrl = "https://냠냠.png";

        // When & Then
        assertThatThrownBy(() -> new DailyTodoCertification(
            dailyTodo,
            certifyContent,
            certifyMediaUrl
        ))
            .isInstanceOf(InvalidDailyTodoCertificationException.class)
            .hasMessage("데일리 투두 인증 내용으로 null 혹은 공백을 입력할 수 없습니다. (%s)", certifyContent);
    }

    @DisplayName("생성자에 유효하지 않은 길이의 데일리 투두 인증 본문을 입력하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenInputInvalidLengthContent() {
        // Given
        final DailyTodo dailyTodo = createDailyTodo(
            ChallengeGroupFixture.create("성욱이와 친구들"),
            createMember(2L, "투두 검사자"),
            DailyTodoStatus.CERTIFY_PENDING,
            LocalDateTime.now()
        );
        final String certifyContent = "A".repeat(MAXIMUM_ALLOWED_CONTENT_LENGTH + 1);
        final String certifyMediaUrl = "https://냠냠.png";

        // When & Then
        assertThatThrownBy(() -> new DailyTodoCertification(
            dailyTodo,
            certifyContent,
            certifyMediaUrl
        ))
            .isInstanceOf(InvalidDailyTodoCertificationException.class)
            .hasMessage(String.format("데일리 투두 인증 내용은 %d자 이하만 입력할 수 있습니다. (%d) (%s)", MAXIMUM_ALLOWED_CONTENT_LENGTH, certifyContent.length(), certifyContent));
    }

    @DisplayName("생성자에 데일리 투두 인증 미디어 url로 null 혹은 공백을 입력하면 예외가 발생한다.")
    @NullAndEmptySource
    @ParameterizedTest
    void throwExceptionWhenInputMediaUrlNullOrEmpty(final String certifyMediaUrl) {
        // Given
        final DailyTodo dailyTodo = createDailyTodo(
            ChallengeGroupFixture.create("성욱이와 친구들"),
            createMember(2L, "투두 검사자"),
            DailyTodoStatus.CERTIFY_PENDING,
            LocalDateTime.now()
        );
        final String certifyContent = "치킨 냠냠 인증!";

        // When & Then
        assertThatThrownBy(() -> new DailyTodoCertification(
            dailyTodo,
            certifyContent,
            certifyMediaUrl
        ))
            .isInstanceOf(InvalidDailyTodoCertificationException.class)
            .hasMessage(String.format("데일리 투두 인증 미디어 url로 null 혹은 공백을 입력할 수 없습니다. (%s)", certifyMediaUrl));
    }
}
