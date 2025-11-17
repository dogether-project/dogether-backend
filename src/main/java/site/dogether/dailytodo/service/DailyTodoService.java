package site.dogether.dailytodo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.service.ChallengeGroupPolicy;
import site.dogether.challengegroup.service.ChallengeGroupReader;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.DailyTodos;
import site.dogether.dailytodo.repository.DailyTodoAndDailyTodoCertification;
import site.dogether.dailytodo.repository.DailyTodoRepository;
import site.dogether.dailytodo.service.dto.DailyTodoDto;
import site.dogether.dailytodo.service.dto.FindMyDailyTodosConditionDto;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus;
import site.dogether.dailytodohistory.service.DailyTodoHistoryService;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static site.dogether.dailytodo.entity.DailyTodoStatus.CERTIFY_PENDING;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DailyTodoService {

    private final ChallengeGroupReader challengeGroupReader;
    private final MemberRepository memberRepository;
    private final DailyTodoRepository dailyTodoRepository;
    private final DailyTodoHistoryService dailyTodoHistoryService;
    private final ChallengeGroupPolicy challengeGroupPolicy;

    @Transactional
    public void saveDailyTodos(
        final Long memberId,
        final Long challengeGroupId,
        final List<String> dailyTodoContents
    ) {
        final Member member = getMember(memberId);
        final ChallengeGroup challengeGroup = getChallengeGroup(challengeGroupId);

        challengeGroupPolicy.validateMemberIsInChallengeGroup(challengeGroup, member);
        challengeGroupPolicy.validateChallengeGroupIsRunning(challengeGroup);

        final DailyTodos dailyTodos = createDailyTodos(challengeGroup, member, dailyTodoContents);
        final List<DailyTodo> savedDailyTodos = dailyTodoRepository.saveAll(dailyTodos.getValues());

        dailyTodoHistoryService.initDailyTodoHistories(savedDailyTodos);
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 id입니다. (%d)", memberId)));
    }

    private ChallengeGroup getChallengeGroup(final Long challengeGroupId) {
        return challengeGroupReader.getById(challengeGroupId);
    }

    private DailyTodos createDailyTodos(
        final ChallengeGroup challengeGroup,
        final Member member,
        final List<String> dailyTodoContents
    ) {
        final List<DailyTodo> dailyTodos = dailyTodoContents.stream()
            .map(content -> new DailyTodo(challengeGroup, member, content))
            .toList();

        return new DailyTodos(dailyTodos);
    }

    public List<String> findYesterdayDailyTodos(final Long memberId, final Long groupId) {
        final ChallengeGroup challengeGroup = getChallengeGroup(groupId);
        final Member member = getMember(memberId);

        final LocalDate yesterdayDate = LocalDate.now().minusDays(1);
        return dailyTodoRepository.findAllByWrittenAtBetweenAndChallengeGroupAndMember(
                yesterdayDate.atStartOfDay(),
                yesterdayDate.atTime(LocalTime.MAX),
                challengeGroup,
                member).stream()
            .map(DailyTodo::getContent)
            .toList();
    }

    public List<DailyTodo> getMemberTodos(final ChallengeGroup challengeGroup, final Member member) {
        return dailyTodoRepository.findAllByChallengeGroupAndMember(challengeGroup, member);
    }

    public List<DailyTodoDto> findMyDailyTodos(final FindMyDailyTodosConditionDto condition) {
        final ChallengeGroup challengeGroup = getChallengeGroup(condition.getGroupId());
        final Member member = getMember(condition.getMemberId());

        return condition.findDailyTodoCertificationReviewStatus()
            .map(status -> findDailyTodosByDailyTodoCertificationReviewStatus(
                challengeGroup,
                member,
                condition.getCreatedAt().atStartOfDay(),
                condition.getCreatedAt().atTime(LocalTime.MAX),
                status))
            .orElse(findDailyTodos(
                challengeGroup,
                member,
                condition.getCreatedAt().atStartOfDay(),
                condition.getCreatedAt().atTime(LocalTime.MAX)));
    }

    private List<DailyTodoDto> findDailyTodosByDailyTodoCertificationReviewStatus(
        final ChallengeGroup challengeGroup,
        final Member member,
        final LocalDateTime startDate,
        final LocalDateTime endDate,
        final String dailyTodoCertificationReviewStatusValue
    ) {
        final DailyTodoCertificationReviewStatus dailyTodoCertificationReviewStatus = DailyTodoCertificationReviewStatus.convertByValue(dailyTodoCertificationReviewStatusValue);
        final List<DailyTodoAndDailyTodoCertification> dailyTodoAndCertificationByCondition = dailyTodoRepository.findAllDailyTodoAndCertificationByReviewResult(
            challengeGroup,
            member,
            startDate,
            endDate,
            dailyTodoCertificationReviewStatus
        );

        return dailyTodoAndCertificationByCondition.stream()
            .map(dailyTodoAndDailyTodoCertification -> {
                final DailyTodo dailyTodo = dailyTodoAndDailyTodoCertification.dailyTodo();
                final DailyTodoCertification dailyTodoCertification = dailyTodoAndDailyTodoCertification.dailyTodoCertification();
                return new DailyTodoDto(dailyTodo, dailyTodoCertification);
            })
            .toList();
    }

    private List<DailyTodoDto> findDailyTodos(
        final ChallengeGroup challengeGroup,
        final Member member,
        final LocalDateTime startDate,
        final LocalDateTime endDate
    ) {
        final List<DailyTodoDto> certifyPendingTodos = new java.util.ArrayList<>(dailyTodoRepository.findAllByChallengeGroupAndMemberAndStatusAndWrittenAtBetweenOrderByWrittenAtDesc(
                challengeGroup,
                member,
                CERTIFY_PENDING,
                startDate,
                endDate)
            .stream()
            .map(DailyTodoDto::new)
            .toList());

        final List<DailyTodoDto> certifyCompletedTodos = dailyTodoRepository.findAllDailyTodoAndCertification(
                challengeGroup,
                member,
                startDate,
                endDate)
            .stream()
            .map(dailyTodoAndDailyTodoCertification -> {
                final DailyTodo dailyTodo = dailyTodoAndDailyTodoCertification.dailyTodo();
                final DailyTodoCertification dailyTodoCertification = dailyTodoAndDailyTodoCertification.dailyTodoCertification();
                return new DailyTodoDto(dailyTodo, dailyTodoCertification);
            })
            .toList();

        certifyPendingTodos.addAll(certifyCompletedTodos);
        return certifyPendingTodos;
    }
}
