package site.dogether.dailytodo.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.fixture.ChallengeGroupFixture;
import site.dogether.challengegroup.exception.ChallengeGroupNotFoundException;
import site.dogether.challengegroup.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.exception.NotRunningChallengeGroupException;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class DailyTodoServiceTest {

    @Autowired private ChallengeGroupRepository challengeGroupRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ChallengeGroupMemberRepository challengeGroupMemberRepository;
    @Autowired private DailyTodoService dailyTodoService;

    private static Member createMember() {
        return new Member(
            null,
            "provider_id",
            "성욱쨩",
            "profile_image_url",
            LocalDateTime.now()
        );
    }

    private static ChallengeGroupMember createChallengeGroupMember(final ChallengeGroup challengeGroup, final Member member) {
        return new ChallengeGroupMember(null, challengeGroup, member, LocalDateTime.now().plusDays(1));
    }

    @DisplayName("유효한 값을 입력하면 데일리 투두를 생성 후 저장한다.")
    @Test
    void saveDailyTodos() {
        // Given
        final Member member = memberRepository.save(createMember());
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroupFixture.create());
        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, member));

        final Long memberId = member.getId();
        final Long challengeGroupId = challengeGroup.getId();
        final List<String> dailyTodoContents = List.of(
            "치킨 먹기",
            "코딩 하기",
            "운동 하기"
        );

        // When & Then
        assertThatCode(() -> dailyTodoService.saveDailyTodos(
            memberId,
            challengeGroupId,
            dailyTodoContents))
            .doesNotThrowAnyException();
    }

    @DisplayName("데일리 투두를 추가로 작성한다.")
    @Test
    void addDailyTodos() {
        // Given
        final Member member = memberRepository.save(createMember());
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroupFixture.create());
        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, member));

        final Long memberId = member.getId();
        final Long challengeGroupId = challengeGroup.getId();
        final List<String> dailyTodoContents = List.of(
                "치킨 먹기",
                "코딩 하기",
                "운동 하기"
        );
        dailyTodoService.saveDailyTodos(
                memberId,
                challengeGroupId,
                dailyTodoContents
        );

        final List<String> additionalDailyTodoContents = List.of("독서 하기", "산책 하기");

        // When & Then
        assertThatCode(() -> dailyTodoService.saveDailyTodos(
            memberId,
            challengeGroupId,
            additionalDailyTodoContents))
            .doesNotThrowAnyException();
    }

    @DisplayName("존재하지 않는 회원 id와 함께 데일리 투두 생성을 요청하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenSaveDailyTodosWithNotFoundMemberId() {
        // Given
        final Member member = memberRepository.save(createMember());
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroupFixture.create());
        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, member));

        final Long memberId = 11324L;
        final Long challengeGroupId = challengeGroup.getId();
        final List<String> dailyTodoContents = List.of(
            "치킨 먹기",
            "코딩 하기",
            "운동 하기"
        );

        // When & Then
        assertThatThrownBy(() -> dailyTodoService.saveDailyTodos(
            memberId,
            challengeGroupId,
            dailyTodoContents))
            .isInstanceOf(MemberNotFoundException.class)
            .hasMessage(String.format("존재하지 않는 회원 id입니다. (%d)", memberId));
    }

    @DisplayName("존재하지 않는 챌린지 그룹 id와 함께 데일리 투두 생성을 요청하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenSaveDailyTodosWithNotFoundChallengeGroupId() {
        // Given
        final Member member = memberRepository.save(createMember());
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroupFixture.create());
        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, member));

        final Long memberId = member.getId();
        final Long challengeGroupId = 11324L;
        final List<String> dailyTodoContents = List.of(
            "치킨 먹기",
            "코딩 하기",
            "운동 하기"
        );

        // When & Then
        assertThatThrownBy(() -> dailyTodoService.saveDailyTodos(
            memberId,
            challengeGroupId,
            dailyTodoContents))
            .isInstanceOf(ChallengeGroupNotFoundException.class)
            .hasMessage(String.format("존재하지 않는 챌린지 그룹 id입니다. (%d)", challengeGroupId));
    }

    @DisplayName("현재 진행중이지 않은 챌린지 그룹에 데일리 투두 생성을 요청하면 예외가 발생한다.")
    @EnumSource(value = ChallengeGroupStatus.class, names = {"READY", "FINISHED"})
    @ParameterizedTest
    void throwExceptionWhenSaveDailyTodosWithNotRunningChallengeGroup(final ChallengeGroupStatus status) {
        // Given
        final Member member = memberRepository.save(createMember());
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroupFixture.create(status));
        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, member));

        final Long memberId = member.getId();
        final Long challengeGroupId = challengeGroup.getId();
        final List<String> dailyTodoContents = List.of(
            "치킨 먹기",
            "코딩 하기",
            "운동 하기"
        );

        // When & Then
        assertThatThrownBy(() -> dailyTodoService.saveDailyTodos(
            memberId,
            challengeGroupId,
            dailyTodoContents))
            .isInstanceOf(NotRunningChallengeGroupException.class)
            .hasMessage(String.format("현재 진행중인 챌린지 그룹이 아닙니다. (%s)", challengeGroup));
    }

    @DisplayName("데일리 투두를 생성하려는 챌린지 그룹에 참여하고 있지 않은 사용자가 요청하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenSaveDailyTodosWhoNotInChallengeGroupMember() {
        // Given
        final Member member = memberRepository.save(createMember());
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroupFixture.create());
        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, member));

        final Member otherMember = new Member(
            null,
            "other_provider_id",
            "이상한 사람",
            "other_profile_image_url",
            LocalDateTime.now()
        );
        memberRepository.save(otherMember);

        final Long memberId = otherMember.getId();
        final Long challengeGroupId = challengeGroup.getId();
        final List<String> dailyTodoContents = List.of(
            "치킨 먹기",
            "코딩 하기",
            "운동 하기"
        );

        // When & Then
        assertThatThrownBy(() -> dailyTodoService.saveDailyTodos(
            memberId,
            challengeGroupId,
            dailyTodoContents))
            .isInstanceOf(MemberNotInChallengeGroupException.class)
            .hasMessage(String.format("사용자가 요청한 챌린지 그룹에 참여중이지 않습니다. (%s) (%s)", challengeGroup, otherMember));
    }
}
