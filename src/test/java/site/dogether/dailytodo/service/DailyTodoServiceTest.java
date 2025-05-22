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
import site.dogether.challengegroup.exception.ChallengeGroupNotFoundException;
import site.dogether.challengegroup.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.exception.NotRunningChallengeGroupException;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodo.exception.DailyTodoAlreadyCreatedException;
import site.dogether.dailytodo.exception.DailyTodoNotFoundException;
import site.dogether.dailytodo.repository.DailyTodoRepository;
import site.dogether.dailytodohistory.entity.DailyTodoHistory;
import site.dogether.dailytodohistory.repository.DailyTodoHistoryRepository;
import site.dogether.fake.FakeRandomGenerator;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;
import site.dogether.memberactivity.entity.DailyTodoStats;
import site.dogether.memberactivity.repository.DailyTodoStatsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static site.dogether.dailytodo.entity.DailyTodoStatus.CERTIFY_PENDING;

@Transactional
@SpringBootTest
class DailyTodoServiceTest {

    @Autowired private ChallengeGroupRepository challengeGroupRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ChallengeGroupMemberRepository challengeGroupMemberRepository;
    @Autowired private DailyTodoRepository dailyTodoRepository;
    @Autowired private DailyTodoStatsRepository dailyTodoStatsRepository;
    @Autowired private DailyTodoHistoryRepository dailyTodoHistoryRepository;
    @Autowired private DailyTodoService dailyTodoService;
    @Autowired private FakeRandomGenerator randomGenerator;

    private static Member createMember() {
        return new Member(
            null,
            "provider_id",
            "성욱쨩",
            "profile_image_url",
            LocalDateTime.now()
        );
    }

    private static Member createMember(final String name) {
        return new Member(
            null,
            "provider_id " + name,
            name,
            "profile_image_url " + name,
            LocalDateTime.now()
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
            ChallengeGroupStatus.RUNNING,
            LocalDateTime.now().plusHours(1)
        );
    }

    private static ChallengeGroup createChallengeGroup(final ChallengeGroupStatus status) {
        return new ChallengeGroup(
            null,
            "성욱이와 친구들",
            8,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            "join_code",
            status,
            LocalDateTime.now().plusHours(1)
        );
    }

    private static ChallengeGroupMember createChallengeGroupMember(final ChallengeGroup challengeGroup, final Member member) {
        return new ChallengeGroupMember(null, challengeGroup, member, LocalDateTime.now().plusDays(1));
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

    @DisplayName("유효한 값을 입력하면 데일리 투두를 생성 후 저장한다.")
    @Test
    void saveDailyTodos() {
        // Given
        final Member member = memberRepository.save(createMember());
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(createChallengeGroup());
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

    @DisplayName("존재하지 않는 회원 id와 함께 데일리 투두 생성을 요청하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenSaveDailyTodosWithNotFoundMemberId() {
        // Given
        final Member member = memberRepository.save(createMember());
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(createChallengeGroup());
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
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(createChallengeGroup());
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
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(createChallengeGroup(status));
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
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(createChallengeGroup());
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

    @DisplayName("데일리 투두를 생성하려는 챌린지 그룹에 오늘 투두를 이미 작성했다면 예외가 발생한다.")
    @Test
    void throwExceptionAlreadyHasTodayTodos() {
        // Given
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(createChallengeGroup());
        final Member member = memberRepository.save(createMember());
        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, member));
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

    @DisplayName("유효한 값을 입력하면 데일리 투두 인증을 생성 후 저장한다.")
    @Test
    void certifyDailyTodoSuccess() {
        // Given
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(createChallengeGroup());
        final Member writer = memberRepository.save(createMember());
        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, writer));
        final DailyTodo dailyTodo = dailyTodoRepository.save(createDailyTodo(
            challengeGroup,
            writer,
            CERTIFY_PENDING,
            null,
            LocalDateTime.now()
        ));
        dailyTodoStatsRepository.save(createDailyTodoStats(writer));
        dailyTodoHistoryRepository.save(createDailyTodoHistory(dailyTodo));

        final Long writerId = writer.getId();
        final Long dailyTodoId = dailyTodo.getId();
        final String certifyContent = "치킨 냠냠 인증!";
        final String certifyMediaUrl = "https://냠냠.png";

        // When && Then
        assertThatCode(() ->  dailyTodoService.certifyDailyTodo(
            writerId,
            dailyTodoId,
            certifyContent,
            certifyMediaUrl))
        .doesNotThrowAnyException();
    }

    @DisplayName("본인 외 다른 멤버도 참여중인 챌린지 그룹에서 유효한 값을 입력하면 데일리 투두 인증을 생성 및 무작위 검사자 선정 후 저장한다.")
    @Test
    void certifyDailyTodoSuccessInManyPeopleExistGroup() {
        // Given
        final ChallengeGroup challengeGroup = challengeGroupRepository.save(createChallengeGroup());
        final Member writer = memberRepository.save(createMember("투두 작성자 본인"));
        final Member otherMember1 = memberRepository.save(createMember("켈리"));
        final Member otherMember2 = memberRepository.save(createMember("폰트"));
        final Member otherMember3 = memberRepository.save(createMember("썬"));
        final Member otherMember4 = memberRepository.save(createMember("개미맨"));
        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, writer));
        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, otherMember1));
        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, otherMember2));
        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, otherMember3));
        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, otherMember4));
        final DailyTodo dailyTodo = dailyTodoRepository.save(createDailyTodo(
            challengeGroup,
            writer,
            CERTIFY_PENDING,
            null,
            LocalDateTime.now()
        ));
        dailyTodoStatsRepository.save(createDailyTodoStats(writer));
        dailyTodoHistoryRepository.save(createDailyTodoHistory(dailyTodo));

        randomGenerator.setResult(2); // 검사자로 썬이 선정되도록 조작

        final Long writerId = writer.getId();
        final Long dailyTodoId = dailyTodo.getId();
        final String certifyContent = "치킨 냠냠 인증!";
        final String certifyMediaUrl = "https://냠냠.png";

        // When && Then
        assertThatCode(() ->  dailyTodoService.certifyDailyTodo(
            writerId,
            dailyTodoId,
            certifyContent,
            certifyMediaUrl))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 데일리 투두에 인증을 하려고 하면 예외가 발생한다.")
    void throwExceptionWhenCertifyNotExistDailyTodo() {
        // Given
        final Member writer = memberRepository.save(createMember());

        final Long writerId = writer.getId();
        final Long dailyTodoId = 12323L;
        final String certifyContent = "치킨 냠냠 인증!";
        final String certifyMediaUrl = "https://냠냠.png";

        // When & Then
        assertThatThrownBy(() ->  dailyTodoService.certifyDailyTodo(
            writerId,
            dailyTodoId,
            certifyContent,
            certifyMediaUrl))
            .isInstanceOf(DailyTodoNotFoundException.class)
            .hasMessage(String.format("존재하지 않는 데일리 투두 id입니다. (%d)", dailyTodoId));
    }

    // TODO : 개발 과정에 필요해서 주석 처리 후 보관
//    @DisplayName("투두 정렬 테스트")
//    @Test
//    void sortTest() {
//        // Given
//        final ChallengeGroup challengeGroup = challengeGroupRepository.save(createChallengeGroup());
//        final Member writer = memberRepository.save(createMember("투두 작성자 본인"));
//        final Member otherMember1 = memberRepository.save(createMember("켈리"));
//        final Member otherMember2 = memberRepository.save(createMember("폰트"));
//        final Member otherMember3 = memberRepository.save(createMember("썬"));
//        final Member otherMember4 = memberRepository.save(createMember("개미맨"));
//        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, writer));
//        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, otherMember1));
//        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, otherMember2));
//        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, otherMember3));
//        challengeGroupMemberRepository.save(createChallengeGroupMember(challengeGroup, otherMember4));
//
//        final DailyTodo dailyTodo1 = dailyTodoRepository.save(createDailyTodo(
//            challengeGroup,
//            writer,
//            REVIEW_PENDING,
//            null,
//            LocalDateTime.now()
//        ));
//        final DailyTodo dailyTodo2 = dailyTodoRepository.save(createDailyTodo(
//            challengeGroup,
//            writer,
//            REVIEW_PENDING,
//            null,
//            LocalDateTime.now()
//        ));
//        final DailyTodo dailyTodo3 = dailyTodoRepository.save(createDailyTodo(
//            challengeGroup,
//            writer,
//            CERTIFY_PENDING,
//            null,
//            LocalDateTime.now()
//        ));
//        final DailyTodo dailyTodo4 = dailyTodoRepository.save(createDailyTodo(
//            challengeGroup,
//            writer,
//            APPROVE,
//            null,
//            LocalDateTime.now()
//        ));
//        final DailyTodo dailyTodo5 = dailyTodoRepository.save(createDailyTodo(
//            challengeGroup,
//            writer,
//            CERTIFY_PENDING,
//            null,
//            LocalDateTime.now()
//        ));
//        final DailyTodo dailyTodo6 = dailyTodoRepository.save(createDailyTodo(
//            challengeGroup,
//            writer,
//            REJECT,
//            "고작 그거야? 풋!",
//            LocalDateTime.now()
//        ));
//        final DailyTodo dailyTodo7 = dailyTodoRepository.save(createDailyTodo(
//            challengeGroup,
//            writer,
//            REJECT,
//            "걍 접어라...",
//            LocalDateTime.now()
//        ));
//
//        dailyTodoCertificationRepository.save(new DailyTodoCertification(
//            null,
//            dailyTodo1,
//            otherMember1,
//            "1번 투두 인증 완료!",
//            "https://인증1.png"
//        ));
//        dailyTodoCertificationRepository.save(new DailyTodoCertification(
//            null,
//            dailyTodo2,
//            otherMember1,
//            "2번 투두 인증 완료!",
//            "https://인증2.png"
//        ));
//        dailyTodoCertificationRepository.save(new DailyTodoCertification(
//            null,
//            dailyTodo4,
//            otherMember2,
//            "4번 투두 인증 완료!",
//            "https://인증4.png"
//        ));
//        dailyTodoCertificationRepository.save(new DailyTodoCertification(
//            null,
//            dailyTodo6,
//            otherMember2,
//            "6번 투두 인증 완료!",
//            "https://인증6.png"
//        ));
//        dailyTodoCertificationRepository.save(new DailyTodoCertification(
//            null,
//            dailyTodo7,
//            otherMember3,
//            "7번 투두 인증 완료!",
//            "https://인증7.png"
//        ));
//
//        // When
//        final List<DailyTodoAndDailyTodoCertificationDto> myDailyTodo = dailyTodoService.findMyDailyTodo(FindMyDailyTodosConditionDto.of(writer.getId(), LocalDate.now(), null));
//        final GetMyDailyTodosResponse result = GetMyDailyTodosResponse.of(myDailyTodo);
//        System.out.println("result = " + result);
//
//        // Then
//    }
}
