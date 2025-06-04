package site.dogether.memberactivity.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus;
import site.dogether.member.entity.Member;
import site.dogether.memberactivity.exception.InvalidDailyTodoStatsException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

class DailyTodoStatsTest {

    private static Member createMember() {
        return new Member(
            1L,
            "provider_id",
            "그로밋",
            "profile_image_url",
            LocalDateTime.now()
        );
    }

    private static DailyTodoStats createDailyTodoStats(Member member) {
        return new DailyTodoStats(
            1L,
            member,
            3,
            2,
            1
        );
    }

    @DisplayName("생성자에 유효한 입력값(id, 멤버, 안증 카운트, 인정 카운트, 노인정 카운트)을 입력하면 DailyTodoStats 인스턴스가 생성된다.")
    @Test
    void createDailyTodoStatsSuccess() {
        //Given
        final Member member = createMember();
        final int certifiedCount = 1;
        final int approvedCount = 2;
        final int rejectedCount = 3;

        //When && Then
        assertThatCode(() -> new DailyTodoStats(
            1L,
            member,
            certifiedCount,
            approvedCount,
            rejectedCount))
            .doesNotThrowAnyException();
    }

    @DisplayName("전체 생성자에 멤버로 null을 입력하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenInputMemberNull_FullConstructor() {
        //Given
        final int certifiedCount = 1;
        final int approvedCount = 2;
        final int rejectedCount = 3;

        //When && Then
        assertThatThrownBy(() -> new DailyTodoStats(
            1L,
            null,
            certifiedCount,
            approvedCount,
            rejectedCount))
            .isInstanceOf(InvalidDailyTodoStatsException.class)
            .hasMessage("데일리 투두 통계 멤버로 null을 입력할 수 없습니다.");
    }

    @DisplayName("생성자에 유효한 일부 입력값(멤버)를 입력하면 기본값과 함께 DailyTodoStats 인스턴스가 생성된다.")
    @Test
    void createDailyTodoStatsWithDefaultValueSuccess() {
        //Given
        final Member member = createMember();

        //When
        final DailyTodoStats dailyTodoStats = new DailyTodoStats(member);

        //Then
        assertSoftly(softly -> {
            assertThat(dailyTodoStats.getId()).isNull();
            assertThat(dailyTodoStats.getCertificatedCount()).isEqualTo(0);
            assertThat(dailyTodoStats.getApprovedCount()).isEqualTo(0);
            assertThat(dailyTodoStats.getRejectedCount()).isEqualTo(0);
        });
    }

    @DisplayName("단일 생성자에 멤버로 null을 입력하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenInputMemberNull_SingleConstructor() {
        //When && Then
        assertThatThrownBy(() -> new DailyTodoStats(null))
            .isInstanceOf(InvalidDailyTodoStatsException.class)
            .hasMessage("데일리 투두 통계 멤버로 null을 입력할 수 없습니다.");
    }

    @DisplayName("인증된 투두 개수를 1 증가한다.")
    @Test
    void whenIncreaseCertificatedCount_thenCountIncreasedByOne() {
        //Given
        final Member member = createMember();
        final DailyTodoStats dailyTodoStats = createDailyTodoStats(member);
        final int certifiedCount = dailyTodoStats.getCertificatedCount();

        //When
        dailyTodoStats.increaseCertificatedCount();

        //Then
        assertThat(dailyTodoStats.getCertificatedCount()).isEqualTo(certifiedCount + 1);
    }

    @DisplayName("데일리 투두 상태가 인정일 때 인정받은 투두 개수를 1 증가한다.")
    @Test
    void whenDailyTodoStatusIsApproved_thenIncreaseApprovedCountByOne() {
        //Given
        final Member member = createMember();
        final DailyTodoStats dailyTodoStats = createDailyTodoStats(member);
        final int approvedCount = dailyTodoStats.getApprovedCount();
        final DailyTodoCertificationReviewStatus status = DailyTodoCertificationReviewStatus.APPROVE;

        //When
        dailyTodoStats.moveCertificatedToResult(status);

        //Then
        assertThat(dailyTodoStats.getApprovedCount()).isEqualTo(approvedCount + 1);
    }

    @DisplayName("데일리 투두 상태가 인정일 때 노인정받은 투두 개수를 1 증가한다.")
    @Test
    void whenDailyTodoStatusIsRejected_thenIncreaseRejectedCountByOne() {
        //Given
        final Member member = createMember();
        final DailyTodoStats dailyTodoStats = createDailyTodoStats(member);
        final int rejectedCount = dailyTodoStats.getRejectedCount();
        final DailyTodoCertificationReviewStatus status = DailyTodoCertificationReviewStatus.REJECT;

        //When
        dailyTodoStats.moveCertificatedToResult(status);

        //Then
        assertThat(dailyTodoStats.getRejectedCount()).isEqualTo(rejectedCount + 1);
    }
}