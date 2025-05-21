package site.dogether.dailytodocertification.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodo.repository.DailyTodoRepository;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.exception.DailyTodoCertificationNotFoundException;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationRepository;
import site.dogether.dailytodohistory.entity.DailyTodoHistory;
import site.dogether.dailytodohistory.repository.DailyTodoHistoryRepository;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;
import site.dogether.memberactivity.entity.DailyTodoStats;
import site.dogether.memberactivity.repository.DailyTodoStatsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static site.dogether.dailytodo.entity.DailyTodoStatus.REVIEW_PENDING;

@Transactional
@SpringBootTest
class DailyTodoCertificationServiceTest {

    @Autowired private ChallengeGroupRepository challengeGroupRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private DailyTodoRepository dailyTodoRepository;
    @Autowired private DailyTodoCertificationRepository dailyTodoCertificationRepository;
    @Autowired private DailyTodoStatsRepository dailyTodoStatsRepository;
    @Autowired private DailyTodoHistoryRepository dailyTodoHistoryRepository;
    @Autowired private DailyTodoCertificationService dailyTodoCertificationService;

    private static ChallengeGroup createChallengeGroup() {
        return new ChallengeGroup(
            null,
            "성욱이와 친구들",
            8,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            "join_code",
            ChallengeGroupStatus.RUNNING,
            LocalDateTime.now().plusHours(1)
        );
    }

    private static Member createMember(final String name) {
        return new Member(
            null,
            "provider_id " + name,
            name,
            "profile_image_url " + name,
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
            null,
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
            null,
            dailyTodo,
            reviewer,
            "인증함!",
            "https://인증.png",
            LocalDateTime.now().plusHours(2)
        );
    }

    private static DailyTodoStats createDailyTodoStats(Member member) {
        return new DailyTodoStats(
                null,
                member,
                1,
                2,
                3
        );
    }

    private static DailyTodoHistory createDailyTodoHistory(final DailyTodo dailyTodo) {
        return new DailyTodoHistory(dailyTodo);
    }
    
    @DisplayName("인정에 대해 유효한 검사 값(검사자 id, 투두 인증 id, 검사 결과, 피드백)을 넘기면 투두 인증 검사를 수행하고 변경된 부분을 DB에 반영 요청 한다.")
    @Test
    void reviewDailyTodoCertificationSuccessInputApprove() {
        // Given
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(createChallengeGroup());
        final Member writer = memberRepository.save(createMember("투두 작성자"));
        final DailyTodo dailyTodo = dailyTodoRepository.save(createDailyTodo(challengeGroup, writer, REVIEW_PENDING, null, LocalDateTime.now()));
        final Member reviewer = memberRepository.save(createMember("인증 검사자"));
        final DailyTodoCertification dailyTodoCertification = dailyTodoCertificationRepository.save(createDailyTodoCertification(dailyTodo, reviewer));
        dailyTodoStatsRepository.save(createDailyTodoStats(writer));
        dailyTodoHistoryRepository.save(createDailyTodoHistory(dailyTodo));

        final Long reviewerId = reviewer.getId();
        final Long dailyTodoCertificationId = dailyTodoCertification.getId();
        final String reviewResult = "approve";
        final String reviewFeedback = "우왕!";

        // When & Then
        assertThatCode(() -> dailyTodoCertificationService.reviewDailyTodoCertification(
            reviewerId,
            dailyTodoCertificationId,
            reviewResult,
            reviewFeedback))
            .doesNotThrowAnyException();
    }

    @DisplayName("노인정에 대해 유효한 검사 값(검사자 id, 투두 인증 id, 검사 결과, 노인정 사유)을 넘기면 투두 인증 검사를 수행하고 변경된 부분을 DB에 반영 요청 한다.")
    @Test
    void reviewDailyTodoCertificationSuccessReject() {
        // Given
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(createChallengeGroup());
        final Member writer = memberRepository.save(createMember("투두 작성자"));
        final DailyTodo dailyTodo = dailyTodoRepository.save(createDailyTodo(challengeGroup, writer, REVIEW_PENDING, null, LocalDateTime.now()));
        final Member reviewer = memberRepository.save(createMember("인증 검사자"));
        final DailyTodoCertification dailyTodoCertification = dailyTodoCertificationRepository.save(createDailyTodoCertification(dailyTodo, reviewer));
        dailyTodoStatsRepository.save(createDailyTodoStats(writer));
        dailyTodoHistoryRepository.save(createDailyTodoHistory(dailyTodo));


        final Long reviewerId = reviewer.getId();
        final Long dailyTodoCertificationId = dailyTodoCertification.getId();
        final String reviewResult = "reject";
        final String reviewFeedback = "이게 최선이야?";

        // When & Then
        assertThatCode(() -> dailyTodoCertificationService.reviewDailyTodoCertification(
            reviewerId,
            dailyTodoCertificationId,
            reviewResult,
            reviewFeedback))
            .doesNotThrowAnyException();
    }

    @DisplayName("존재하지 않는 투두 인증 검사자 id로 인증 검사 요청을 하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenInputNotFoundReviewerId() {
        // Given
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(createChallengeGroup());
        final Member writer = memberRepository.save(createMember("투두 작성자"));
        final DailyTodo dailyTodo = dailyTodoRepository.save(createDailyTodo(challengeGroup, writer, REVIEW_PENDING, null, LocalDateTime.now()));
        final Member reviewer = memberRepository.save(createMember("인증 검사자"));
        final DailyTodoCertification dailyTodoCertification = dailyTodoCertificationRepository.save(createDailyTodoCertification(dailyTodo, reviewer));

        final Long reviewerId = 1232L;
        final Long dailyTodoCertificationId = dailyTodoCertification.getId();
        final String reviewResult = "reject";
        final String reviewFeedback = "이게 최선이야?";

        // When & Then
        assertThatThrownBy(() -> dailyTodoCertificationService.reviewDailyTodoCertification(
            reviewerId,
            dailyTodoCertificationId,
            reviewResult,
            reviewFeedback
        ))
            .isInstanceOf(MemberNotFoundException.class)
            .hasMessage(String.format("존재하지 않는 회원 id입니다. (%d)", reviewerId));
    }

    @DisplayName("존재하지 않는 투두 인증 id로 인증 검사 요청을 하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenInputNotFoundDailyTodoCertificationId() {
        // Given
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(createChallengeGroup());
        final Member writer = memberRepository.save(createMember("투두 작성자"));
        final DailyTodo dailyTodo = dailyTodoRepository.save(createDailyTodo(challengeGroup, writer, REVIEW_PENDING, null, LocalDateTime.now()));
        final Member reviewer = memberRepository.save(createMember("인증 검사자"));
        dailyTodoCertificationRepository.save(createDailyTodoCertification(dailyTodo, reviewer));

        final Long reviewerId = reviewer.getId();
        final Long dailyTodoCertificationId = 1231231L;
        final String reviewResult = "reject";
        final String reviewFeedback = "이게 최선이야?";

        // When & Then
        assertThatThrownBy(() -> dailyTodoCertificationService.reviewDailyTodoCertification(
            reviewerId,
            dailyTodoCertificationId,
            reviewResult,
            reviewFeedback
        ))
            .isInstanceOf(DailyTodoCertificationNotFoundException.class)
            .hasMessage(String.format("존재하지 않는 데일리 투두 인증 id입니다. (%d)", dailyTodoCertificationId));
    }
}
