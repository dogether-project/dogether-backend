package site.dogether.dailytodo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.dailytodo.exception.InvalidDailyTodoException;
import site.dogether.dailytodo.exception.NotCertifyPendingDailyTodoException;
import site.dogether.dailytodo.exception.NotCreatedTodayDailyTodoException;
import site.dogether.dailytodo.exception.NotDailyTodoWriterException;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus;
import site.dogether.member.entity.Member;
import site.dogether.memberactivity.entity.DailyTodoStats;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static site.dogether.dailytodo.entity.DailyTodo.MAXIMUM_ALLOWED_CONTENT_LENGTH;
import static site.dogether.dailytodo.entity.DailyTodoStatus.CERTIFY_COMPLETED;
import static site.dogether.dailytodo.entity.DailyTodoStatus.CERTIFY_PENDING;

class DailyTodoTest {

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

    private static Member createMember() {
        return new Member(
            1L,
            "provider_id",
            "성욱쨩",
            "profile_image_url",
            LocalDateTime.now()
        );
    }

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

    private static DailyTodoCertification createDailyTodoCertification(
        final DailyTodo dailyTodo,
        final DailyTodoCertificationReviewStatus reviewStatus,
        final String reviewFeedback
    ) {
        return new DailyTodoCertification(
            1L,
            dailyTodo,
            "인증함!",
            "https://인증.png",
            reviewStatus,
            reviewFeedback,
            LocalDateTime.now().plusHours(2)
        );
    }

    private static DailyTodoStats createDailyTodoStats(Member member) {
        return new DailyTodoStats(
                1L,
                member,
                1,
                2,
                3
        );
    }

    @DisplayName("생성자에 유효한 입력값(id, 챌린지 그룹, 멤버, 투두 내용, 투두 상태, 노인정 사유)을 입력하면 DailyTodo 인스턴스가 생성된다.")
    @Test
    void createDailyTodoSuccess() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member member = createMember();
        final String content = "치킨 먹기";
        final DailyTodoStatus status = CERTIFY_PENDING;
        final LocalDateTime writtenAt = LocalDateTime.now();

        // When && Then
        assertThatCode(() -> new DailyTodo(
            1L,
            challengeGroup,
            member,
            content,
            status,
            writtenAt))
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
        });
    }

    @DisplayName("생성자에 챌린지 그룹으로 null을 입력하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenInputChallengeGroupNull() {
        // Given
        final Member member = createMember();
        final String content = "치킨 먹기";

        // When & Then
        assertThatThrownBy(() -> new DailyTodo(null, member, content))
            .isInstanceOf(InvalidDailyTodoException.class)
            .hasMessage("데일리 투두 챌린지 그룹으로 null을 입력할 수 없습니다.");
    }

    @DisplayName("생성자에 작성자로 null을 입력하면 예외가 발생한다.")
    @Test
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
            .hasMessage(String.format("데일리 투두 내용은 %d자 이하만 입력할 수 있습니다. (%d) (%s)", MAXIMUM_ALLOWED_CONTENT_LENGTH, content.length(), content));
    }

    @DisplayName("생성자에 데일리 투두 상태로 null을 입력하면 예외가 발생한다.")
    @Test()
    void throwExceptionWhenInputStatusNull() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member member = createMember();
        final String content = "치킨 먹기";
        final LocalDateTime writtenAt = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> new DailyTodo(1L, challengeGroup, member, content, null, writtenAt))
            .isInstanceOf(InvalidDailyTodoException.class)
            .hasMessage("데일리 투두 상태로 null을 입력할 수 없습니다.");
    }

    @DisplayName("생성자에 데일리 투두 작성일로 null을 입력하면 예외가 발생한다.")
    @Test()
    void throwExceptionWhenInputWrittenAtNull() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member member = createMember();
        final String content = "치킨 먹기";
        final DailyTodoStatus status = CERTIFY_PENDING;

        // When & Then
        assertThatThrownBy(() -> new DailyTodo(1L, challengeGroup, member, content, status, null))
            .isInstanceOf(InvalidDailyTodoException.class)
            .hasMessage("데일리 투두 작성일로 null을 입력할 수 없습니다.");
    }

    @DisplayName("데일리 투두 상태가 인증 대기중이면 true를 반환한다.")
    @Test
    void returnTrueWhenStatusIsCertifyPending() {
        // Given
        final DailyTodo dailyTodo = createDailyTodo(
            createChallengeGroup(),
            createMember(),
            CERTIFY_PENDING,
            LocalDateTime.now()
        );

        // When
        final boolean isCertifyPending = dailyTodo.isCertifyPending();

        // Then
        assertThat(isCertifyPending).isTrue();
    }

    @DisplayName("데일리 투두 상태가 인증 대기중이 아니면 false를 반환한다.")
    @Test
    void returnFalseWhenStatusIsNotCertifyPending() {
        // Given
        final DailyTodo dailyTodo = createDailyTodo(
            createChallengeGroup(),
            createMember(),
            CERTIFY_COMPLETED,
            LocalDateTime.now()
        );

        // When
        final boolean isCertifyPending = dailyTodo.isCertifyPending();

        // Then
        assertThat(isCertifyPending).isFalse();
    }

    @DisplayName("유효한 입력값(투두 작성자, 인증 본문, 인증 미디어 url)을 넘기면 데일리 투두 인증을 생성한다.")
    @Test
    void certifySuccess() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member writer = createMember(1L, "투두 작성자");
        final DailyTodo dailyTodo = createDailyTodo(
            challengeGroup,
            writer,
            CERTIFY_PENDING,
            LocalDateTime.now()
        );
        final DailyTodoStats dailyTodoStats = createDailyTodoStats(writer);

        final String certifyContent = "치킨 야무지게 먹은거 인증!";
        final String certifyMediaUrl = "https://치킨_냠냠.png";

        // When
        final DailyTodoCertification dailyTodoCertification = dailyTodo.certify(writer, certifyContent, certifyMediaUrl, dailyTodoStats);

        // Then
        assertThat(dailyTodoCertification).isNotNull();
        assertThat(dailyTodo.getStatus()).isEqualTo(CERTIFY_COMPLETED);
    }

    @DisplayName("투두 작성자가 아닌 회원이 데일리투두 인증을 생성하려고 하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenCertifyNotWriter() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member writer = createMember(1L, "투두 작성자");
        final DailyTodo dailyTodo = createDailyTodo(
            challengeGroup,
            writer,
            CERTIFY_PENDING,
            LocalDateTime.now()
        );
        final DailyTodoStats dailyTodoStats = createDailyTodoStats(writer);

        final String certifyContent = "치킨 야무지게 먹은거 인증!";
        final String certifyMediaUrl = "https://치킨_냠냠.png";

        final Member otherMember = createMember(3L, "이상한 사람");

        // When & Then
        assertThatThrownBy(() -> dailyTodo.certify(otherMember, certifyContent, certifyMediaUrl, dailyTodoStats))
            .isInstanceOf(NotDailyTodoWriterException.class)
            .hasMessage(String.format("데일리 투두 작성자 외에는 투두 인증을 생성할 수 없습니다. (%s) (%s)", dailyTodo, otherMember));
    }

    @DisplayName("인증 대기 상태가 아닌 투두를 인증 하려고 하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenCertifyNotCertifyPendingStatus() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member writer = createMember(1L, "투두 작성자");
        final DailyTodo dailyTodo = createDailyTodo(
            challengeGroup,
            writer,
            CERTIFY_COMPLETED,
            LocalDateTime.now()
        );
        final DailyTodoStats dailyTodoStats = createDailyTodoStats(writer);

        final String certifyContent = "치킨 야무지게 먹은거 인증!";
        final String certifyMediaUrl = "https://치킨_냠냠.png";

        // When & Then
        assertThatThrownBy(() -> dailyTodo.certify(writer, certifyContent, certifyMediaUrl, dailyTodoStats))
            .isInstanceOf(NotCertifyPendingDailyTodoException.class)
            .hasMessage(String.format("인증 대기 상태가 아닌 데일리 투두는 인증을 생성할 수 없습니다. (%s)", dailyTodo));
    }

    @DisplayName("오늘 작성되지 않은 데일리 투두에 인증을 생성하려고 하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenNotCreatedToday() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member writer = createMember(1L, "투두 작성자");
        final DailyTodo dailyTodo = createDailyTodo(
            challengeGroup,
            writer,
            CERTIFY_PENDING,
            LocalDateTime.now().minusDays(1)
        );
        final DailyTodoStats dailyTodoStats = createDailyTodoStats(writer);

        final String certifyContent = "치킨 야무지게 먹은거 인증!";
        final String certifyMediaUrl = "https://치킨_냠냠.png";

        // When & Then
        assertThatThrownBy(() -> dailyTodo.certify(writer, certifyContent, certifyMediaUrl, dailyTodoStats))
            .isInstanceOf(NotCreatedTodayDailyTodoException.class)
            .hasMessage(String.format("데일리 투두가 작성된 당일에만 투두 인증을 생성할 수 있습니다. (%s)", dailyTodo));
    }
}
