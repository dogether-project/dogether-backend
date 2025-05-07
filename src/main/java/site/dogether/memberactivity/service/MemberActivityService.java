package site.dogether.memberactivity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;
import site.dogether.challengegroup.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.challengegroup.service.ChallengeGroupService;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodo.entity.MyTodoSummary;
import site.dogether.dailytodo.repository.DailyTodoRepository;
import site.dogether.dailytodo.service.DailyTodoService;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationRepository;
import site.dogether.member.entity.Member;
import site.dogether.member.service.MemberService;
import site.dogether.memberactivity.InvalidParameterException;
import site.dogether.memberactivity.controller.response.GetGroupActivityStatResponse;
import site.dogether.memberactivity.controller.response.GetMemberAllStatsResponse;
import site.dogether.memberactivity.repository.DailyTodoStatsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberActivityService {

    private final MemberService memberService;
    private final ChallengeGroupRepository challengeGroupRepository;
    private final ChallengeGroupMemberRepository challengeGroupMemberRepository;
    private final DailyTodoRepository dailyTodoRepository;
    private final DailyTodoCertificationRepository dailyTodoCertificationRepository;
    private final DailyTodoStatsRepository dailyTodoStatsRepository;
    private final ChallengeGroupService challengeGroupService;
    private final DailyTodoService dailyTodoService;

    public GetGroupActivityStatResponse getGroupActivityStat(final Long memberId, final Long groupId) {
        final Member member = memberService.getMember(memberId);

        final ChallengeGroup challengeGroup = challengeGroupRepository.findById(groupId)
                .orElseThrow(() -> new InvalidChallengeGroupException("해당 그룹이 존재하지 않습니다"));

        if (challengeGroup.isFinished()) {
            throw new InvalidChallengeGroupException(String.format("이미 종료된 그룹입니다. (%s)", challengeGroup.getName()));
        }

        if (!challengeGroupMemberRepository.existsByChallengeGroupAndMember(challengeGroup, member)) {
            throw new MemberNotInChallengeGroupException("그룹에 속해있지 않은 유저입니다.");
        }

        final List<ChallengeGroupMember> groupMembers = challengeGroupMemberRepository.findAllByChallengeGroup(challengeGroup);

        return new GetGroupActivityStatResponse(
                getChallengeGroupInfo(challengeGroup),
                getCertificationPeriods(member, challengeGroup),
                getMyRank(memberId, groupMembers, challengeGroup),
                getMemberStats(member, challengeGroup)
        );
    }

    public GetGroupActivityStatResponse.ChallengeGroupInfoResponse getChallengeGroupInfo(final ChallengeGroup challengeGroup) {
        final int currentMemberCount = challengeGroupMemberRepository.countByChallengeGroup(challengeGroup);

        final String endAt = challengeGroup.getEndAt()
                .format(DateTimeFormatter.ofPattern("yy.MM.dd"));

        return new GetGroupActivityStatResponse.ChallengeGroupInfoResponse(
                challengeGroup.getName(),
                challengeGroup.getMaximumMemberCount(),
                currentMemberCount,
                challengeGroup.getJoinCode(),
                endAt
        );
    }

    public List<GetGroupActivityStatResponse.CertificationPeriodResponse> getCertificationPeriods(final Member member, final ChallengeGroup challengeGroup) {
        List<GetGroupActivityStatResponse.CertificationPeriodResponse> result = new ArrayList<>();
        final LocalDate today = LocalDate.now();
        final LocalDate groupStartAt = challengeGroup.getStartAt();

        for (int i = 3; i >= 0; i--) {
            final LocalDate targetDate = today.minusDays(i);

            final int day = (int) ChronoUnit.DAYS.between(groupStartAt, targetDate) + 1;

            if (!targetDate.isBefore(groupStartAt) && !targetDate.isAfter(challengeGroup.getEndAt())) {
                result.add(certificationPeriod(day, targetDate, member, challengeGroup));
            }
        }

        return result;
    }

    public GetGroupActivityStatResponse.CertificationPeriodResponse certificationPeriod(final int day, final LocalDate date, final Member member, final ChallengeGroup challengeGroup) {
        final LocalDateTime startOfDay = date.atStartOfDay();
        final LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        final List<DailyTodo> todos = dailyTodoRepository.findAllByChallengeGroupAndMemberAndCreatedAtBetween(
                challengeGroup,
                member,
                startOfDay,
                endOfDay
        );

        final int createdCount = todos.size();

        final int certificatedCount = (int) todos.stream()
                .filter(todo -> todo.getStatus() == DailyTodoStatus.APPROVE)
                .count();

        final int certificationRate = calculateCertificationRate(createdCount, certificatedCount);

        return new GetGroupActivityStatResponse.CertificationPeriodResponse(
                day,
                createdCount,
                certificatedCount,
                certificationRate
        );
    }

    private int calculateCertificationRate(final int createdCount, final int certificatedCount) {
        if (createdCount == 0) {
            return 0;
        }
        return (int) ((double) (certificatedCount / createdCount) * 100);
    }

    public GetGroupActivityStatResponse.RankingResponse getMyRank(final Long memberId, final List<ChallengeGroupMember> groupMembers, final ChallengeGroup challengeGroup) {
        final int totalMemberCount = challengeGroupMemberRepository.countByChallengeGroup(challengeGroup);
        final int myRank = challengeGroupService.getMyRank(memberId, groupMembers, challengeGroup);

        return new GetGroupActivityStatResponse.RankingResponse(totalMemberCount, myRank);
    }

    public GetGroupActivityStatResponse.MemberStatsResponse getMemberStats(final Member member, final ChallengeGroup challengeGroup) {
        final List<DailyTodo> myTodos = dailyTodoService.getMemberTodos(challengeGroup, member);
        final MyTodoSummary myTodoSummary = new MyTodoSummary(myTodos);

        return new GetGroupActivityStatResponse.MemberStatsResponse(
                myTodoSummary.calculateTotalCertificatedCount(),
                myTodoSummary.calculateTotalApprovedCount(),
                myTodoSummary.calculateTotalRejectedCount()
        );
    }

    public GetMemberAllStatsResponse getMemberAllStats(Long memberId, String sort, String status) {
        final Member member = memberService.getMember(memberId);

        GetMemberAllStatsResponse.DailyTodoStats stats = getStats(member);
        List<DailyTodoCertification> certifications = getCertificationsByStatus(member, status);

        Object dailyTodoCertifications = switch (sort) {
            case "TODO_COMPLETED_AT" -> getCertificationsSortedByTodoCompletedAt(member, certifications, status);
            case "GROUP_CREATED_AT" -> getCertificationsSortedByGroupCreatedAt(member, certifications, status);
            default -> throw new InvalidParameterException("유효하지 않은 sort 파라미터입니다.");
        };

        return new GetMemberAllStatsResponse(stats, dailyTodoCertifications);
    }

    private GetMemberAllStatsResponse.DailyTodoStats getStats(Member member) {
        return dailyTodoStatsRepository.findByMember(member)
                .map(stats -> new GetMemberAllStatsResponse.DailyTodoStats(
                        stats.getCertificatedCount(),
                        stats.getApprovedCount(),
                        stats.getRejectedCount()
                ))
                .orElseGet(() -> new GetMemberAllStatsResponse.DailyTodoStats(0, 0, 0));
    }

    private List<DailyTodoCertification> getCertificationsByStatus(Member member, String status) {
        return Optional.ofNullable(status)
                .filter(s -> !s.isBlank())
                .map(DailyTodoStatus::convertFromValue)
                .map(s -> dailyTodoCertificationRepository.findAllByDailyTodo_MemberAndDailyTodo_Status(member, s))
                .orElseGet(() -> dailyTodoCertificationRepository.findAllByDailyTodo_Member(member));
    }

    private List<GetMemberAllStatsResponse.CertificationsSortByTodoCompletedAt> getCertificationsSortedByTodoCompletedAt(Member member, List<DailyTodoCertification> certifications, String status) {
        return certifications.stream()
                .collect(Collectors.groupingBy(certification -> certification.getCreatedAt().toLocalDate().toString()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new GetMemberAllStatsResponse.CertificationsSortByTodoCompletedAt(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(this::certificationInfo)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    private List<GetMemberAllStatsResponse.CertificationsSortByGroupCreatedAt> getCertificationsSortedByGroupCreatedAt(Member member, List<DailyTodoCertification> certifications, String status) {
        return certifications.stream()
                .collect(Collectors.groupingBy(certification -> certification.getDailyTodo().getChallengeGroup().getName()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new GetMemberAllStatsResponse.CertificationsSortByGroupCreatedAt(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(this::certificationInfo)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    private GetMemberAllStatsResponse.DailyTodoCertificationInfo certificationInfo(DailyTodoCertification certification) {
        DailyTodo todo = certification.getDailyTodo();

        return new GetMemberAllStatsResponse.DailyTodoCertificationInfo(
                todo.getId(),
                todo.getContent(),
                todo.getStatus().name(),
                certification.getContent(),
                certification.getMediaUrl(),
                todo.getRejectReason().orElse(null)
        );
    }
}
