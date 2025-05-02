package site.dogether.dailytodo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.exception.ChallengeGroupNotFoundException;
import site.dogether.challengegroup.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.exception.NotRunningChallengeGroupException;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.DailyTodos;
import site.dogether.dailytodo.exception.DailyTodoAlreadyCreatedException;
import site.dogether.dailytodo.exception.DailyTodoNotFoundException;
import site.dogether.dailytodo.repository.DailyTodoRepository;
import site.dogether.dailytodo.service.dto.DailyTodoAndDailyTodoCertificationDto;
import site.dogether.dailytodo.service.dto.FindMyDailyTodosConditionDto;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.exception.DailyTodoCertificationNotFoundException;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationRepository;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;
import site.dogether.member.service.MemberService;
import site.dogether.notification.service.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import static site.dogether.dailytodo.entity.DailyTodoStatus.CERTIFY_PENDING;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DailyTodoService {

    private final ChallengeGroupRepository challengeGroupRepository;
    private final MemberRepository memberRepository;
    private final ChallengeGroupMemberRepository challengeGroupMemberRepository;
    private final DailyTodoRepository dailyTodoRepository;
    private final DailyTodoCertificationRepository dailyTodoCertificationRepository;
    private final NotificationService notificationService;
    private final MemberService memberService;
    private final ReviewerPicker reviewerPicker;

    @Transactional
    public void saveDailyTodos(
        final Long memberId,
        final Long challengeGroupId,
        final List<String> dailyTodoContents
    ) {
        final Member member = getMember(memberId);
        final ChallengeGroup challengeGroup = getChallengeGroup(challengeGroupId);

        validateMemberIsInChallengeGroup(challengeGroup, member);
        validateChallengeGroupIsRunning(challengeGroup);
        validateMemberHasCreatedDailyTodoToday(challengeGroup, member);

        final DailyTodos dailyTodos = createDailyTodos(challengeGroup, member, dailyTodoContents);
        dailyTodoRepository.saveAll(dailyTodos.getValues());
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 id입니다. (%d)", memberId)));
    }

    private ChallengeGroup getChallengeGroup(final Long challengeGroupId) {
        return challengeGroupRepository.findById(challengeGroupId)
            .orElseThrow(() -> new ChallengeGroupNotFoundException(String.format("존재하지 않는 챌린지 그룹 id입니다. (%d)", challengeGroupId)));
    }

    private void validateChallengeGroupIsRunning(final ChallengeGroup challengeGroup) {
        if (!challengeGroup.isRunning()) {
            throw new NotRunningChallengeGroupException(String.format("현재 진행중인 챌린지 그룹이 아닙니다. (%s)", challengeGroup));
        }
    }

    private void validateMemberIsInChallengeGroup(final ChallengeGroup challengeGroup, final Member member) {
        if (!challengeGroupMemberRepository.existsByChallengeGroupAndMember(challengeGroup, member)) {
            throw new MemberNotInChallengeGroupException(String.format("사용자가 요청한 챌린지 그룹에 참여중이지 않습니다. (%s) (%s)", challengeGroup, member));
        }
    }

    private void validateMemberHasCreatedDailyTodoToday(final ChallengeGroup challengeGroup, final Member member) {
        final boolean createdDailyTodoToday = dailyTodoRepository.existsByChallengeGroupAndMemberAndCreatedAtBetween(
            challengeGroup,
            member,
            LocalDate.now().atStartOfDay(),
            LocalDate.now().atTime(LocalTime.MAX)
        );

        if (createdDailyTodoToday) {
            throw new DailyTodoAlreadyCreatedException(String.format("사용자가 해당 챌린지 그룹에 오늘 작성한 투두가 이미 존재합니다. (%s) (%s)", challengeGroup, member));
        }
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

    @Transactional
    public void certifyDailyTodo(
        final Long memberId,
        final Long dailyTodoId,
        final String certifyContent,
        final String certifyMediaUrl
    ) {
        final Member writer = getMember(memberId);
        final DailyTodo dailyTodo = getDailyTodo(dailyTodoId);
        final ChallengeGroup challengeGroup = dailyTodo.getChallengeGroup();

        validateMemberIsInChallengeGroup(challengeGroup, writer);
        validateChallengeGroupIsRunning(challengeGroup);

        final Member reviewer = reviewerPicker.pickReviewerInChallengeGroup(challengeGroup, writer).orElse(null);
        final DailyTodoCertification dailyTodoCertification = dailyTodo.certify(writer, reviewer, certifyContent, certifyMediaUrl);
        dailyTodoCertificationRepository.save(dailyTodoCertification);

        sendNotificationToReviewer(reviewer, writer, dailyTodo);
    }

    private DailyTodo getDailyTodo(final Long dailyTodoId) {
        return dailyTodoRepository.findById(dailyTodoId)
            .orElseThrow(() -> new DailyTodoNotFoundException(String.format("존재하지 않는 데일리 투두 id입니다. (%d)", dailyTodoId)));
    }

    private void sendNotificationToReviewer(
        final Member reviewer,
        final Member writer,
        final DailyTodo dailyTodo
    ) {
        if (reviewer == null) {
            return;
        }

        notificationService.sendNotification(
            reviewer.getId(),
            String.format("%s님의 투두 인증 검사자로 선정되었습니다.", writer.getName()),
            String.format("투두 내용 : " + dailyTodo.getContent()),
            "CERTIFICATION"
        );
    }

    public List<String> findYesterdayDailyTodos(final Long memberId) {
        final Member member = memberService.getMember(memberId);
        final ChallengeGroupMember challengeGroupMember = challengeGroupMemberRepository.findByChallengeGroup_StatusAndMember(ChallengeGroupStatus.RUNNING, member)
            .orElseThrow(() -> new MemberNotInChallengeGroupException("현재 진행중인 챌린지 그룹에 참여하고 있지 않습니다."));

        final LocalDate yesterday = LocalDate.now().minusDays(1);
        return dailyTodoRepository.findAllByCreatedAtBetweenAndChallengeGroupAndMember(
            yesterday.atStartOfDay(),
            yesterday.atTime(LocalTime.MAX),
            challengeGroupMember.getChallengeGroup(),
            challengeGroupMember.getMember())
            .stream()
            .map(DailyTodo::getContent)
            .toList();
    }

/*    public MyTodoSummary getMyTodoSummary(final Member member, final ChallengeGroup joiningGroup) {
        final List<DailyTodo> dailyTodos = dailyTodoRepository.findAllByChallengeGroupAndMember(joiningGroup, member);
        return new MyTodoSummary(dailyTodos, member.getName());
    }*/

    public List<DailyTodo> getMemberTodos(final ChallengeGroup challengeGroup, final Member member) {
        return dailyTodoRepository.findAllByChallengeGroupAndMember(challengeGroup, member);
    }

    public List<DailyTodoAndDailyTodoCertificationDto> findMyDailyTodo(final FindMyDailyTodosConditionDto condition) {
        final Member member = memberService.getMember(condition.getMemberId());
        final List<DailyTodo> dailyTodos = condition.getDailyTodoStatus()
            .map(status -> dailyTodoRepository.findAllByMemberAndCreatedAtBetweenAndStatus(
                member,
                condition.getCreatedAt().atStartOfDay(),
                condition.getCreatedAt().atTime(LocalTime.MAX),
                status))
            .orElse(getSortingDailyTodos(
                member,
                condition.getCreatedAt().atStartOfDay(),
                condition.getCreatedAt().atTime(LocalTime.MAX)));

        return dailyTodos.stream()
            .map(this::convertToDto)
            .toList();
    }

    private List<DailyTodo> getSortingDailyTodos(final Member member, final LocalDateTime start, final LocalDateTime end) {
        return dailyTodoRepository.findAllByMemberAndCreatedAtBetween(member, start, end).stream()
            .sorted(Comparator.comparing((DailyTodo todo) -> todo.getStatus() != CERTIFY_PENDING)
                .thenComparing(DailyTodo::getId))
            .toList();
    }

    private DailyTodoAndDailyTodoCertificationDto convertToDto(final DailyTodo dailyTodo) {
        if (dailyTodo.getStatus() == CERTIFY_PENDING) {
            return DailyTodoAndDailyTodoCertificationDto.withoutDailyTodoCertification(dailyTodo);
        }

        final DailyTodoCertification dailyTodoCertification = dailyTodoCertificationRepository.findByDailyTodo(dailyTodo)
            .orElseThrow(() -> new DailyTodoCertificationNotFoundException("데일리 투두 인증이 존재하지 않습니다."));
        return new DailyTodoAndDailyTodoCertificationDto(dailyTodo, dailyTodoCertification);
    }
}
