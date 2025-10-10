package site.dogether.dailyTodoHistory.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.fixture.ChallengeGroupFixture;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.DailyTodos;
import site.dogether.dailytodo.repository.DailyTodoRepository;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationRepository;
import site.dogether.dailytodohistory.entity.DailyTodoHistory;
import site.dogether.dailytodohistory.repository.DailyTodoHistoryRepository;
import site.dogether.dailytodohistory.service.DailyTodoHistoryService;
import site.dogether.dailytodohistory.service.dto.FindTargetMemberTodayTodoHistoriesDto;
import site.dogether.member.entity.Member;
import site.dogether.member.repository.MemberRepository;
import site.dogether.memberactivity.entity.DailyTodoStats;
import site.dogether.memberactivity.repository.DailyTodoStatsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Transactional
@SpringBootTest
public class DailyTodoHistoryServiceTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private ChallengeGroupRepository challengeGroupRepository;
    @Autowired private DailyTodoRepository dailyTodoRepository;
    @Autowired private DailyTodoHistoryRepository dailyTodoHistoryRepository;
    @Autowired private DailyTodoCertificationRepository dailyTodoCertificationRepository;
    @Autowired private DailyTodoStatsRepository dailyTodoStatsRepository;
    @Autowired private DailyTodoHistoryService dailyTodoHistoryService;

    private static Member createMember(final String name) {
        return new Member(
                null,
                "provider_id" + name,
                name,
                "profile_image_url " + name,
                LocalDateTime.now()
        );
    }

    private static DailyTodoStats createDailyTodoStats(final Member member) {
        return new DailyTodoStats(member);
    }

    private static DailyTodos createDailyTodos(
            final ChallengeGroup challengeGroup,
            final Member member,
            final List<String> dailyTodoContents
    ) {
        final List<DailyTodo> dailyTodos = dailyTodoContents.stream()
                .map(content -> new DailyTodo(challengeGroup, member, content))
                .toList();

        return new DailyTodos(dailyTodos);
    }

    private static List<DailyTodoHistory> createDailyTodoHistories(final List<DailyTodo> dailyTodos) {
        return dailyTodos.stream()
                .map(DailyTodoHistory::new)
                .toList();
    }

    private DailyTodoCertification createDailyTodoCertification(
            final DailyTodo dailyTodo,
            final Member dailyTodoWriter,
            final String certifyContent,
            final String certifyMediaUrl,
            final DailyTodoStats dailyTodoStats
    ) {
        return dailyTodo.certify(dailyTodoWriter, certifyContent, certifyMediaUrl, dailyTodoStats);
    }

    @DisplayName("참여중인 특정 챌린지 그룹에 속한 특정 그룹원의 당일 데일리 투두 히스토리 전체 조회")
    @Test
    void findAllTodayTodoHistories() {
        // Given
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroupFixture.create("성욱이와 친구들"));
        final Member viewer = memberRepository.save(createMember("히스토리_조회자"));
        final Member targetMember = memberRepository.save(createMember("히스토리_조회_대상자"));
        DailyTodoStats dailyTodoStats = dailyTodoStatsRepository.save(createDailyTodoStats(targetMember));

        List<DailyTodo> dailyTodos = dailyTodoRepository.saveAll(createDailyTodos(
                challengeGroup,
                targetMember,
                List.of("치킨 먹기", "코딩 하기", "운동 하기")
        ).getValues());
        dailyTodoHistoryRepository.saveAll(createDailyTodoHistories(dailyTodos));

        DailyTodoCertification dailyTodoCertification = dailyTodoCertificationRepository.save(createDailyTodoCertification(
                dailyTodos.get(0),
                targetMember,
                "치킨 먹었습니다! 맛있어요!",
                "chicken.url",
                dailyTodoStats
        ));

        dailyTodoCertification.review(DailyTodoCertificationReviewStatus.APPROVE, "맛있는 치킨은 인정합니다..");

        // When
        FindTargetMemberTodayTodoHistoriesDto targetMemberTodayTodoHistories = dailyTodoHistoryService.findAllTodayTodoHistories(
                viewer.getId(),
                challengeGroup.getId(),
                targetMember.getId()
        );

        // Then
        assertSoftly(softly -> {
            softly.assertThat(targetMemberTodayTodoHistories.currentTodoHistoryToReadIndex()).isEqualTo(0);
            assertThat(targetMemberTodayTodoHistories.todoHistories().get(0).content()).isEqualTo("치킨 먹기");
            assertThat(targetMemberTodayTodoHistories.todoHistories().get(0).status()).isEqualTo(DailyTodoCertificationReviewStatus.APPROVE.name());
            assertThat(targetMemberTodayTodoHistories.todoHistories().get(0).certificationContent()).isEqualTo("치킨 먹었습니다! 맛있어요!");
            assertThat(targetMemberTodayTodoHistories.todoHistories().get(0).certificationMediaUrl()).isEqualTo("chicken.url");
            assertThat(targetMemberTodayTodoHistories.todoHistories().get(0).isRead()).isFalse();
            assertThat(targetMemberTodayTodoHistories.todoHistories().get(0).reviewFeedback()).isEqualTo("맛있는 치킨은 인정합니다..");
        });
    }
}
