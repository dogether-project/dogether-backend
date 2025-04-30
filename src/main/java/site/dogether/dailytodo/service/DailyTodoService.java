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
import site.dogether.challengegroup.exception.NotEnoughChallengeGroupMembersException;
import site.dogether.challengegroup.exception.NotRunningChallengeGroupException;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodo.entity.DailyTodos;
import site.dogether.dailytodo.entity.MyTodoSummary;
import site.dogether.dailytodo.exception.*;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DailyTodoService {

    private static final Random random = new Random(); // TODO : 테스트를 위해 추상화

    private final ChallengeGroupRepository challengeGroupRepository;
    private final MemberRepository memberRepository;
    private final ChallengeGroupMemberRepository challengeGroupMemberRepository;
    private final DailyTodoRepository dailyTodoRepository;
    private final DailyTodoCertificationRepository dailyTodoCertificationRepository;
    private final NotificationService notificationService;
    private final MemberService memberService;

    @Transactional
    public void saveDailyTodos(
        final Long memberId,
        final Long challengeGroupId,
        final List<String> dailyTodoContents
    ) {
        final Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 id입니다. (%d)", memberId)));
        final ChallengeGroup challengeGroup = challengeGroupRepository.findById(challengeGroupId)
            .orElseThrow(() -> new ChallengeGroupNotFoundException(String.format("존재하지 않는 챌린지 그룹 id입니다. (%d)", challengeGroupId)));

        validateChallengeGroupIsRunning(challengeGroup);
        validateMemberIsInChallengeGroup(challengeGroup, member);
        validateMemberHasCreatedDailyTodoToday(challengeGroup, member);

        final DailyTodos dailyTodos = createDailyTodos(challengeGroup, member, dailyTodoContents);
        dailyTodoRepository.saveAll(dailyTodos.getValues());
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
        final DailyTodo dailyTodo = getDailyTodo(dailyTodoId);
        final Member member = memberService.getMember(memberId);

        dailyTodo.validateWriter(member);

        final ChallengeGroup challengeGroup = dailyTodo.getChallengeGroup();
        validateChallengeGroupIsRunning(challengeGroup);
        checkDailyTodoStatusIsCertifyPending(dailyTodo);
        checkDailyTodoCreatedToday(dailyTodo);

        final DailyTodoCertification dailyTodoCertification = DailyTodoCertification.create(certifyContent, dailyTodo, member, certifyMediaUrl);
        final Member dailyTodoCertificationReviewer = pickDailyTodoCertificationReviewer(challengeGroup, member);
        dailyTodoCertificationRepository.save(dailyTodoCertification);

        dailyTodo.changeStatusReviewPending();

        notificationService.sendNotification(
            dailyTodoCertificationReviewer.getId(),
            member.getName() + "님의 투두 수행 검사자로 배정되었습니다!",
            "투두 내용 : " + dailyTodo.getContent(),
            "CERTIFICATION"
        );
    }

    private DailyTodo getDailyTodo(final Long dailyTodoId) {
        return dailyTodoRepository.findById(dailyTodoId)
            .orElseThrow(() -> new DailyTodoNotFoundException(String.format("존재하지 않는 데일리 투두 정보입니다. (dailyTodoId : %d)", dailyTodoId)));
    }

    private void checkDailyTodoStatusIsCertifyPending(final DailyTodo dailyTodo) {
        if (!dailyTodo.isCertifyPending()) {
            throw new DailyTodoStatusException(String.format("데일리 투두가 인증 대기 상태가 아닙니다. (%s)", dailyTodo));
        }
    }

    private void checkDailyTodoCreatedToday(final DailyTodo dailyTodo) {
        if (!dailyTodo.createdToday()) {
            throw new DailyTodoCreatedDateException(String.format("오늘 작성한 데일리 투두만 인증할 수 있습니다. (%s)", dailyTodo));
        }
    }

    private Member pickDailyTodoCertificationReviewer(final ChallengeGroup challengeGroup, final Member certifyingMember) {
        final List<Member> otherChallengeGroupMembers = new ArrayList<>(
            challengeGroupMemberRepository.findAllByChallengeGroup(challengeGroup)
                .stream()
                .map(ChallengeGroupMember::getMember)
                .filter(member -> !member.getId().equals(certifyingMember.getId())) // targetMemberId를 가진 멤버 제외
                .toList()
        );

        if (otherChallengeGroupMembers.isEmpty()) {
            throw new NotEnoughChallengeGroupMembersException(String.format("챌린지 그룹에 사용자가 존재하지 않습니다. (%s)", challengeGroup));
        }

        final Member pickedReviewer = otherChallengeGroupMembers.get(random.nextInt(otherChallengeGroupMembers.size()));
        log.info("데일리 투두 수행 인증 검사자 배정 완료 : 투두 수행 인증자 - {}, 투두 수행 검사자 - {}", certifyingMember, pickedReviewer);
        return pickedReviewer;
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

    public MyTodoSummary getMyTodoSummary(final Member member, final ChallengeGroup joiningGroup) {
        final List<DailyTodo> dailyTodos = dailyTodoRepository.findAllByChallengeGroupAndMember(joiningGroup, member);
        return new MyTodoSummary(dailyTodos, member.getName());
    }

    public List<MyTodoSummary> getMyTodoSummaries(final List<Member> groupMembers, final ChallengeGroup joiningGroupEntity) {
        return groupMembers.stream()
                .map(memberJpaEntity -> getMyTodoSummary(memberJpaEntity, joiningGroupEntity))
                .toList();
    }

    public List<DailyTodoAndDailyTodoCertificationDto> findMyDailyTodo(final FindMyDailyTodosConditionDto condition) {
        final Member member = memberService.getMember(condition.getMemberId());
        final List<DailyTodo> dailyTodos = condition.getDailyTodoStatus()
            .map(status -> dailyTodoRepository.findAllByMemberAndCreatedAtBetweenAndStatus(
                member,
                condition.getCreatedAt().atStartOfDay(),
                condition.getCreatedAt().atTime(LocalTime.MAX),
                status))
            .orElse(dailyTodoRepository.findAllByMemberAndCreatedAtBetween(
                member,
                condition.getCreatedAt().atStartOfDay(),
                condition.getCreatedAt().atTime(LocalTime.MAX)));

        return dailyTodos.stream()
            .map(this::convertToDto)
            .toList();
    }

    private DailyTodoAndDailyTodoCertificationDto convertToDto(final DailyTodo dailyTodo) {
        if (dailyTodo.getStatus() == DailyTodoStatus.CERTIFY_PENDING) {
            return DailyTodoAndDailyTodoCertificationDto.of(dailyTodo);
        }

        final DailyTodoCertification dailyTodoCertification = dailyTodoCertificationRepository.findByDailyTodo(dailyTodo)
            .orElseThrow(() -> new DailyTodoCertificationNotFoundException("데일리 투두 인증이 존재하지 않습니다."));
        return new DailyTodoAndDailyTodoCertificationDto(dailyTodo, dailyTodoCertification);
    }
}
