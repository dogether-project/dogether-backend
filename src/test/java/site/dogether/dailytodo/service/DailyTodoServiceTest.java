package site.dogether.dailytodo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.exception.ChallengeGroupNotFoundException;
import site.dogether.challengegroup.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.exception.NotRunningChallengeGroupException;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.dailytodo.exception.DailyTodoAlreadyCreatedException;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;

import java.time.LocalDate;
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
            "profile_image_url"
        );
    }

    private static ChallengeGroup createChallengeGroup() {
        return new ChallengeGroup(
            null,
            "성욱이와 친구들",
            8,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            "join_code",
            ChallengeGroupStatus.RUNNING);
    }

    private static ChallengeGroup createChallengeGroup(final ChallengeGroupStatus status) {
        return new ChallengeGroup(
            null,
            "성욱이와 친구들",
            8,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            "join_code",
            status);
    }

    private static ChallengeGroupMember createChallengeGroupMember(final ChallengeGroup challengeGroup, final Member member) {
        return new ChallengeGroupMember(null, challengeGroup, member);
    }

    @DisplayName("유효한 값을 입력하면 데일리 투두를 생성 후 저장한다.")
    @Test
    void saveDailyTodos() {
        // Given
        final Member member = createMember();
        final ChallengeGroup challengeGroup = createChallengeGroup();
        memberRepository.save(member);
        challengeGroupRepository.save(challengeGroup);

        final ChallengeGroupMember challengeGroupMember = createChallengeGroupMember(challengeGroup, member);
        challengeGroupMemberRepository.save(challengeGroupMember);

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

    @DisplayName("존재하지 않는 회원 id와 함께 데일리 투두 생성을 요청하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenSaveDailyTodosWithNotFoundMemberId() {
        // Given
        final Member member = createMember();
        final ChallengeGroup challengeGroup = createChallengeGroup();
        memberRepository.save(member);
        challengeGroupRepository.save(challengeGroup);

        final ChallengeGroupMember challengeGroupMember = createChallengeGroupMember(challengeGroup, member);
        challengeGroupMemberRepository.save(challengeGroupMember);

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
        final Member member = createMember();
        final ChallengeGroup challengeGroup = createChallengeGroup();
        memberRepository.save(member);
        challengeGroupRepository.save(challengeGroup);

        final ChallengeGroupMember challengeGroupMember = createChallengeGroupMember(challengeGroup, member);
        challengeGroupMemberRepository.save(challengeGroupMember);

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
        final Member member = createMember();
        final ChallengeGroup challengeGroup = createChallengeGroup(status);
        memberRepository.save(member);
        challengeGroupRepository.save(challengeGroup);

        final ChallengeGroupMember challengeGroupMember = createChallengeGroupMember(challengeGroup, member);
        challengeGroupMemberRepository.save(challengeGroupMember);

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
        final Member member = createMember();
        final ChallengeGroup challengeGroup = createChallengeGroup();
        memberRepository.save(member);
        challengeGroupRepository.save(challengeGroup);

        final ChallengeGroupMember challengeGroupMember = createChallengeGroupMember(challengeGroup, member);
        challengeGroupMemberRepository.save(challengeGroupMember);

        final Member otherMember = new Member(
            null,
            "other_provider_id",
            "이상한 사람",
            "other_profile_image_url");
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

    @DisplayName("데일리 투두를 생성하려는 챌린지 그룹에 오늘 투두를 이미 작성했다면 예외가 발생한다.")
    @Test
    void throwExceptionAlreadyHasTodayTodos() {
        // Given
        final ChallengeGroup challengeGroup = createChallengeGroup();
        final Member member = createMember();
        challengeGroupRepository.save(challengeGroup);
        memberRepository.save(member);

        final ChallengeGroupMember challengeGroupMember = createChallengeGroupMember(challengeGroup, member);
        challengeGroupMemberRepository.save(challengeGroupMember);

        dailyTodoService.saveDailyTodos(
            member.getId(),
            challengeGroup.getId(),
            List.of("이미 투두 작성", "이따 또 적어야징 히히", "아이 재밌다 낄낄"));

        final Long memberId = member.getId();
        final Long challengeGroupId = challengeGroup.getId();
        final List<String> dailyTodoContents = List.of("새로운 투두", "또 적으려고 왔습니당 히히");

        // When & Then
        assertThatThrownBy(() -> dailyTodoService.saveDailyTodos(memberId, challengeGroupId, dailyTodoContents))
            .isInstanceOf(DailyTodoAlreadyCreatedException.class)
            .hasMessage("사용자가 해당 챌린지 그룹에 오늘 작성한 투두가 이미 존재합니다. (%s) (%s)", challengeGroup, member);
    }
}
