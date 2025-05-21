package site.dogether.dailytodocertification.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodocertification.exception.InvalidDailyTodoCertificationException;
import site.dogether.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static site.dogether.dailytodocertification.entity.DailyTodoCertification.MAXIMUM_ALLOWED_CONTENT_LENGTH;

class DailyTodoCertificationTest {

    private static ChallengeGroup createChallengeGroup() {
        return new ChallengeGroup(
            1L,
            "성욱이와 친구들",
            8,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            "join_code",
            ChallengeGroupStatus.RUNNING,
            LocalDateTime.now().plusHours(1)
        );
    }

    private static Member createMember(final Long id, final String name) {
        return new Member(
            id,
            "provider_id",
            name,
            "profile_image_url",
            LocalDateTime.now().plusHours(0)
        );
    }

    private static DailyTodo createDailyTodo(
        final ChallengeGroup challengeGroup,
        final Member member,
        final DailyTodoStatus status,
        final String reviewFeedback,
        final LocalDateTime writtenAt
    ) {
        return new DailyTodo(
            1L,
            challengeGroup,
            member,
            "치킨 먹기",
            status,
            reviewFeedback,
            writtenAt
        );
    }

    private static DailyTodoCertification createDailyTodoCertification(
        final DailyTodo dailyTodo,
        final Member reviewer
    ) {
        return new DailyTodoCertification(
            1L,
            dailyTodo,
            reviewer,
            "인증함!",
            "https://인증.png",
            LocalDateTime.now().plusHours(2)
        );
    }

    @DisplayName("생성자에 유효한 입력값을 넘기면 DailyTodoCertification 인스턴스가 생성된다.")
    @Test
    void createSuccess() {
        // Given
        final DailyTodo dailyTodo = createDailyTodo(
            createChallengeGroup(),
            createMember(1L, "투두 작성자"),
            DailyTodoStatus.CERTIFY_PENDING,
            null,
            LocalDateTime.now()
        );
        final Member reviewer = createMember(2L, "투두 검사자");
        final String certifyContent = "치킨 냠냠 인증!";
        final String certifyMediaUrl = "https://냠냠.png";

        // When & Then
        assertThatCode(() -> new DailyTodoCertification(
            null,
            dailyTodo,
            reviewer,
            certifyContent,
            certifyMediaUrl,
            LocalDateTime.now().plusHours(2)
        ))
        .doesNotThrowAnyException();
    }

    @DisplayName("생성자에 데일리 투두로 null을 입력하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenInputDailyTodoNull() {
        // Given
        final Member reviewer = createMember(2L, "투두 검사자");
        final String certifyContent = "치킨 냠냠 인증!";
        final String certifyMediaUrl = "https://냠냠.png";

        // When & Then
        assertThatThrownBy(() -> new DailyTodoCertification(
            null,
            null,
            reviewer,
            certifyContent,
            certifyMediaUrl,
            LocalDateTime.now().plusHours(2)
        ))
            .isInstanceOf(InvalidDailyTodoCertificationException.class)
            .hasMessage("데일리 투두 인증 생성에 데일리 투두로 null을 입력할 수 없습니다.");
    }

    @DisplayName("생성자에 인증 검사자로 투두 작성자 본인을 입력하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenInputReviewerDailyTodoWriter() {
        // Given
        final Member writer = createMember(1L, "투두 작성자");

        final DailyTodo dailyTodo = createDailyTodo(
            createChallengeGroup(),
            writer,
            DailyTodoStatus.CERTIFY_PENDING,
            null,
            LocalDateTime.now()
        );
        final Member writerMyself = createMember(1L, "투두 작성자");
        final String certifyContent = "치킨 냠냠 인증!";
        final String certifyMediaUrl = "https://냠냠.png";

        // When & Then
        assertThatThrownBy(() -> new DailyTodoCertification(
            null,
            dailyTodo,
            writerMyself,
            certifyContent,
            certifyMediaUrl,
            LocalDateTime.now().plusHours(2)
        ))
            .isInstanceOf(InvalidDailyTodoCertificationException.class)
            .hasMessage(String.format("데일리 투두 인증 검사자로 본인을 지정할 수 없습니다. (%s)", writerMyself));
    }

    @DisplayName("생성자에 데일리 투두 인증 본문으로 null 혹은 공백을 입력하면 예외가 발생한다.")
    @NullAndEmptySource
    @ParameterizedTest
    void throwExceptionWhenInputContentNullOrEmpty(final String certifyContent) {
        // Given
        final Member writer = createMember(1L, "투두 작성자");
        final DailyTodo dailyTodo = createDailyTodo(
            createChallengeGroup(),
            createMember(2L, "투두 검사자"),
            DailyTodoStatus.CERTIFY_PENDING,
            null,
            LocalDateTime.now()
        );
        final String certifyMediaUrl = "https://냠냠.png";

        // When & Then
        assertThatThrownBy(() -> new DailyTodoCertification(
            null,
            dailyTodo,
            writer,
            certifyContent,
            certifyMediaUrl,
            LocalDateTime.now().plusHours(2)
        ))
            .isInstanceOf(InvalidDailyTodoCertificationException.class)
            .hasMessage("데일리 투두 인증 내용으로 null 혹은 공백을 입력할 수 없습니다. (%s)", certifyContent);
    }

    @DisplayName("생성자에 유효하지 않은 길이의 데일리 투두 인증 본문을 입력하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenInputInvalidLengthContent() {
        // Given
        final Member writer = createMember(1L, "투두 작성자");
        final DailyTodo dailyTodo = createDailyTodo(
            createChallengeGroup(),
            createMember(2L, "투두 검사자"),
            DailyTodoStatus.CERTIFY_PENDING,
            null,
            LocalDateTime.now()
        );
        final String certifyContent = "A".repeat(MAXIMUM_ALLOWED_CONTENT_LENGTH + 1);
        final String certifyMediaUrl = "https://냠냠.png";

        // When & Then
        assertThatThrownBy(() -> new DailyTodoCertification(
            null,
            dailyTodo,
            writer,
            certifyContent,
            certifyMediaUrl,
            LocalDateTime.now().plusHours(2)
        ))
            .isInstanceOf(InvalidDailyTodoCertificationException.class)
            .hasMessage(String.format("데일리 투두 인증 내용은 %d자 이하만 입력할 수 있습니다. (%d) (%s)", MAXIMUM_ALLOWED_CONTENT_LENGTH, certifyContent.length(), certifyContent));
    }

    @DisplayName("생성자에 데일리 투두 인증 미디어 url로 null 혹은 공백을 입력하면 예외가 발생한다.")
    @NullAndEmptySource
    @ParameterizedTest
    void throwExceptionWhenInputMediaUrlNullOrEmpty(final String certifyMediaUrl) {
        // Given
        final Member writer = createMember(1L, "투두 작성자");
        final DailyTodo dailyTodo = createDailyTodo(
            createChallengeGroup(),
            createMember(2L, "투두 검사자"),
            DailyTodoStatus.CERTIFY_PENDING,
            null,
            LocalDateTime.now()
        );
        final String certifyContent = "치킨 냠냠 인증!";

        // When & Then
        assertThatThrownBy(() -> new DailyTodoCertification(
            null,
            dailyTodo,
            writer,
            certifyContent,
            certifyMediaUrl,
            LocalDateTime.now().plusHours(2)
        ))
            .isInstanceOf(InvalidDailyTodoCertificationException.class)
            .hasMessage(String.format("데일리 투두 인증 미디어 url로 null 혹은 공백을 입력할 수 없습니다. (%s)", certifyMediaUrl));
    }
    
    @DisplayName("데일리 투두 인증의 검사자가 맞으면 true를 반환한다.")
    @Test
    void returnTrueWhenIsReviewer() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member writer = createMember(1L, "투두 작성자");
        final DailyTodo dailyTodo = createDailyTodo(challengeGroup, writer, DailyTodoStatus.REVIEW_PENDING, null, LocalDateTime.now().minusHours(2));
        final Member reviewer = createMember(2L, "인증 검사자");
        final DailyTodoCertification dailyTodoCertification = createDailyTodoCertification(dailyTodo, reviewer);

        // When
        final boolean isReviewer = dailyTodoCertification.isReviewer(reviewer);

        // Then
        assertThat(isReviewer).isTrue();
    }

    @DisplayName("데일리 투두 인증의 검사자가 아니면 false를 반환한다.")
    @Test
    void returnFalseWhenIsNotReviewer() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member writer = createMember(1L, "투두 작성자");
        final DailyTodo dailyTodo = createDailyTodo(challengeGroup, writer, DailyTodoStatus.REVIEW_PENDING, null, LocalDateTime.now().minusHours(2));
        final Member reviewer = createMember(2L, "인증 검사자");
        final DailyTodoCertification dailyTodoCertification = createDailyTodoCertification(dailyTodo, reviewer);

        final Member otherMember = createMember(3L, "이상한 사람");

        // When
        final boolean isReviewer = dailyTodoCertification.isReviewer(otherMember);

        // Then
        assertThat(isReviewer).isFalse();
    }
}
