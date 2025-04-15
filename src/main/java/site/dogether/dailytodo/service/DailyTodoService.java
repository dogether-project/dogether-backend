package site.dogether.dailytodo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.exception.NotEnoughChallengeGroupMembersException;
import site.dogether.challengegroup.exception.NotRunningChallengeGroupException;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.MyTodoSummary;
import site.dogether.dailytodo.exception.*;
import site.dogether.dailytodo.repository.DailyTodoRepository;
import site.dogether.dailytodo.service.dto.DailyTodoAndDailyTodoCertificationDto;
import site.dogether.dailytodo.service.dto.FindMyDailyTodosConditionDto;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationMediaUrl;
import site.dogether.dailytodocertification.exception.DailyTodoCertificationNotFoundException;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationMediaUrlRepository;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationRepository;
import site.dogether.member.entity.Member;
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

    private final ChallengeGroupMemberRepository challengeGroupMemberRepository;
    private final DailyTodoRepository dailyTodoRepository;
    private final DailyTodoCertificationRepository dailyTodoCertificationRepository;
    private final DailyTodoCertificationMediaUrlRepository dailyTodoCertificationMediaUrlRepository;
    private final NotificationService notificationService;
    private final MemberService memberService;

    @Transactional
    public void saveDailyTodo(final Long memberId, final List<String> dailyTodoContents) {
        final Member member = memberService.getMember(memberId);
        final ChallengeGroupMember challengeGroupMember = getRunningChallengeGroupMemberEntity(member);
        checkExistPendingReviewDailyTodos(member);

        final ChallengeGroup challengeGroup = challengeGroupMember.getChallengeGroup();
//        challengeGroup.checkEnableTodoCount(dailyTodoContents.size());
        final List<DailyTodo> dailyTodos = createDailyTodos(dailyTodoContents, challengeGroupMember);
        dailyTodoRepository.saveAll(dailyTodos);
    }

    private ChallengeGroupMember getRunningChallengeGroupMemberEntity(final Member member) {
        return challengeGroupMemberRepository.findByChallengeGroup_StatusAndMember(ChallengeGroupStatus.RUNNING, member)
            .orElseThrow(() -> new MemberNotInChallengeGroupException(String.format("현재 진행중이지 않거나 존재하지 않는 챌린지 그룹입니다. (member : %s)", member)));
    }

    private void checkExistPendingReviewDailyTodos(final Member member) {
        final boolean existPendingReviewDailyTodos = dailyTodoCertificationRepository.existsByDailyTodo_StatusAndReviewer(DailyTodoStatus.REVIEW_PENDING, member);
        if (existPendingReviewDailyTodos) {
            throw new UnreviewedDailyTodoExistsException("아직 검사하지 않은 데일리 투두가 존재합니다.");
        }
    }

    private List<DailyTodo> createDailyTodos(final List<String> dailyTodoContents, final ChallengeGroupMember challengeGroupMember) {
        return dailyTodoContents.stream()
            .map(dailyTodoContent -> DailyTodo.create(
                dailyTodoContent,
                challengeGroupMember.getMember(),
                challengeGroupMember.getChallengeGroup()))
            .toList();
    }

    @Transactional
    public void certifyDailyTodo(
        final Long memberId,
        final Long dailyTodoId,
        final String certifyContent,
        final List<String> certifyMediaUrls
    ) {
        final DailyTodo dailyTodo = getDailyTodo(dailyTodoId);
        final Member member = memberService.getMember(memberId);

        checkDailyTodoOwner(dailyTodo, member);

        final ChallengeGroup challengeGroup = dailyTodo.getChallengeGroup();
        checkChallengeGroupIsRunning(challengeGroup);
        checkDailyTodoStatusIsCertifyPending(dailyTodo);
        checkDailyTodoCreatedToday(dailyTodo);

        final DailyTodoCertification dailyTodoCertification = DailyTodoCertification.create(certifyContent, dailyTodo, member);
        final Member dailyTodoCertificationReviewer = pickDailyTodoCertificationReviewer(challengeGroup, member);
        dailyTodoCertificationRepository.save(dailyTodoCertification);

        final List<DailyTodoCertificationMediaUrl> dailyTodoCertificationMediaUrls = certifyMediaUrls.stream()
            .map(mediaUrlValue -> DailyTodoCertificationMediaUrl.create(mediaUrlValue, dailyTodoCertification))
            .toList();
        dailyTodoCertificationMediaUrlRepository.saveAll(dailyTodoCertificationMediaUrls);
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

    private void checkDailyTodoOwner(final DailyTodo dailyTodo, final Member member) {
        if (!dailyTodo.checkOwner(member)) {
            throw new NotDailyTodoOwnerException(String.format("요청자가 작성한 데일리 투두가 아닙니다. (dailyTodo : %s, member : %s)", dailyTodo, member));
        }
    }

    private void checkChallengeGroupIsRunning(final ChallengeGroup challengeGroup) {
        if (!challengeGroup.isRunning()) {
            throw new NotRunningChallengeGroupException(String.format("현재 진행중인 챌린지 그룹이 아닙니다. (%s)", challengeGroup));
        }
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
        final List<DailyTodoCertificationMediaUrl> certificationMediaUrls = dailyTodoCertificationMediaUrlRepository.findAllByDailyTodoCertification(dailyTodoCertification);

        return new DailyTodoAndDailyTodoCertificationDto(
            dailyTodo,
            dailyTodoCertification,
            certificationMediaUrls
        );
    }
}
