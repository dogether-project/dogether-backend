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
import site.dogether.dailytodo.repository.DailyTodoRepository;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationCount;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationRepository;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;
import site.dogether.memberactivity.controller.v1.dto.response.GetGroupActivityStatApiResponseV1;
import site.dogether.memberactivity.controller.v0.dto.response.GetMemberAllStatsApiResponseV0;
import site.dogether.memberactivity.entity.DailyTodoStats;
import site.dogether.memberactivity.exception.InvalidParameterException;
import site.dogether.memberactivity.repository.DailyTodoStatsRepository;
import site.dogether.memberactivity.service.dto.FindMyProfileDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberActivityService {

    private final ChallengeGroupRepository challengeGroupRepository;
    private final ChallengeGroupMemberRepository challengeGroupMemberRepository;
    private final DailyTodoRepository dailyTodoRepository;
    private final DailyTodoCertificationRepository dailyTodoCertificationRepository;
    private final DailyTodoStatsRepository dailyTodoStatsRepository;
    private final MemberRepository memberRepository;
    private final ChallengeGroupService challengeGroupService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public void initDailyTodoStats(Member member) {
        DailyTodoStats stats = new DailyTodoStats(member);
        dailyTodoStatsRepository.save(stats);
    }

    public GetGroupActivityStatApiResponseV1 getGroupActivityStat(final Long memberId, final Long groupId) {
        final Member member = getMember(memberId);

        final ChallengeGroup challengeGroup = challengeGroupRepository.findById(groupId)
                .orElseThrow(() -> new InvalidChallengeGroupException("해당 그룹이 존재하지 않습니다"));

        if (challengeGroup.isFinished()) {
            throw new InvalidChallengeGroupException(String.format("이미 종료된 그룹입니다. (%s)", challengeGroup.getName()));
        }

        if (!challengeGroupMemberRepository.existsByChallengeGroupAndMember(challengeGroup, member)) {
            throw new MemberNotInChallengeGroupException("그룹에 속해있지 않은 유저입니다.");
        }

        return new GetGroupActivityStatApiResponseV1(
                getChallengeGroupInfo(challengeGroup),
                getCertificationPeriods(member, challengeGroup),
                getMyRank(member, challengeGroup),
                getMemberGroupStats(member, challengeGroup)
        );
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 id입니다. (%d)", memberId)));
    }

    public GetGroupActivityStatApiResponseV1.ChallengeGroupInfoResponse getChallengeGroupInfo(final ChallengeGroup challengeGroup) {
        final int currentMemberCount = challengeGroupMemberRepository.countByChallengeGroup(challengeGroup);

        final String endAt = challengeGroup.getEndAt()
                .format(DateTimeFormatter.ofPattern("yy.MM.dd"));

        return new GetGroupActivityStatApiResponseV1.ChallengeGroupInfoResponse(
                challengeGroup.getName(),
                challengeGroup.getMaximumMemberCount(),
                currentMemberCount,
                challengeGroup.getJoinCode(),
                endAt
        );
    }

    public List<GetGroupActivityStatApiResponseV1.CertificationPeriodResponse> getCertificationPeriods(final Member member, final ChallengeGroup challengeGroup) {
        List<GetGroupActivityStatApiResponseV1.CertificationPeriodResponse> result = new ArrayList<>();
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

    public GetGroupActivityStatApiResponseV1.CertificationPeriodResponse certificationPeriod(final int day, final LocalDate date, final Member member, final ChallengeGroup challengeGroup) {
        final LocalDateTime startOfDay = date.atStartOfDay();
        final LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        final List<DailyTodo> todos = dailyTodoRepository.findAllByChallengeGroupAndMemberAndWrittenAtBetween(
                challengeGroup,
                member,
                startOfDay,
                endOfDay
        );

        final int createdCount = todos.size();

        final int certificatedCount = (int) todos.stream()
                .filter(DailyTodo::isCertifyCompleted)
                .count();

        final int certificationRate = calculateCertificationRate(createdCount, certificatedCount);

        return new GetGroupActivityStatApiResponseV1.CertificationPeriodResponse(
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
        return (int) (((double) certificatedCount / createdCount) * 100);
    }

    public GetGroupActivityStatApiResponseV1.RankingResponse getMyRank(final Member target, final ChallengeGroup challengeGroup) {
        final List<ChallengeGroupMember> groupMembers = challengeGroupMemberRepository.findAllByChallengeGroup(challengeGroup);

        final int totalMemberCount = challengeGroupMemberRepository.countByChallengeGroup(challengeGroup);
        final int myRank = challengeGroupService.getMyRank(target, groupMembers);

        return new GetGroupActivityStatApiResponseV1.RankingResponse(totalMemberCount, myRank);
    }

    public GetGroupActivityStatApiResponseV1.MemberStatsResponse getMemberGroupStats(final Member member, final ChallengeGroup challengeGroup) {
        final DailyTodoCertificationCount dailyTodoCertificationCount = dailyTodoCertificationRepository.countDailyTodoCertification(challengeGroup, member);

        return new GetGroupActivityStatApiResponseV1.MemberStatsResponse(
            dailyTodoCertificationCount.getTotalCount(),
            dailyTodoCertificationCount.getApprovedCount(),
            dailyTodoCertificationCount.getRejectedCount()
        );
    }

    //TODO: 추후 로직 개선을 위한 리팩토링 진행 예정
    public GetMemberAllStatsApiResponseV0 getMemberAllStats(Long memberId, String sort, String status) {
        final Member member = getMember(memberId);

        GetMemberAllStatsApiResponseV0.DailyTodoStats stats = getStats(member);
        List<DailyTodoCertification> certifications = getCertificationsByStatus(member, status);

        if ("TODO_COMPLETED_AT".equals(sort)) {
            List<GetMemberAllStatsApiResponseV0.CertificationsGroupedByTodoCompletedAt> groupedCertifications =
                    getCertificationsSortedByTodoCompletedAt(certifications);
            return new GetMemberAllStatsApiResponseV0(stats, groupedCertifications, null);
        }

        if ("GROUP_CREATED_AT".equals(sort)) {
            List<GetMemberAllStatsApiResponseV0.CertificationsGroupedByGroupCreatedAt> groupedCertifications =
                    getCertificationsSortedByGroupCreatedAt(certifications);
            return new GetMemberAllStatsApiResponseV0(stats, null, groupedCertifications);
        }

        throw new InvalidParameterException("유효하지 않은 sort 파라미터입니다.");
    }

    private GetMemberAllStatsApiResponseV0.DailyTodoStats getStats(Member member) {
        return dailyTodoStatsRepository.findByMember(member)
                .map(stats -> new GetMemberAllStatsApiResponseV0.DailyTodoStats(
                        stats.getCertificatedCount(),
                        stats.getApprovedCount(),
                        stats.getRejectedCount()
                ))
                .orElseGet(() -> new GetMemberAllStatsApiResponseV0.DailyTodoStats(0, 0, 0));
    }

    private List<DailyTodoCertification> getCertificationsByStatus(Member member, String status) {
        if (status != null && !status.isBlank()) {
            final DailyTodoCertificationReviewStatus dailyTodoCertificationReviewStatus = DailyTodoCertificationReviewStatus.convertByValue(status);
            return dailyTodoCertificationRepository.findAllByDailyTodo_MemberAndReviewStatus(member, dailyTodoCertificationReviewStatus);
        }

        return dailyTodoCertificationRepository.findAllByDailyTodo_Member(member);
    }

    private List<GetMemberAllStatsApiResponseV0.CertificationsGroupedByTodoCompletedAt> getCertificationsSortedByTodoCompletedAt(List<DailyTodoCertification> certifications) {
        return certifications.stream()
                .collect(Collectors.groupingBy(cert -> cert.getCreatedAt().toLocalDate().format(DATE_FORMATTER)))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .map(entry -> new GetMemberAllStatsApiResponseV0.CertificationsGroupedByTodoCompletedAt(
                        entry.getKey(),
                        entry.getValue().stream()
                                .sorted(Comparator.comparing(DailyTodoCertification::getCreatedAt).reversed())
                                .map(this::certificationInfo)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    private List<GetMemberAllStatsApiResponseV0.CertificationsGroupedByGroupCreatedAt> getCertificationsSortedByGroupCreatedAt(List<DailyTodoCertification> certifications) {
        return certifications.stream()
                .collect(Collectors.groupingBy(certification -> certification.getDailyTodo().getChallengeGroup()))
                .entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getCreatedAt(), Comparator.reverseOrder()))
                .map(entry -> new GetMemberAllStatsApiResponseV0.CertificationsGroupedByGroupCreatedAt(
                        entry.getKey().getName(),
                        entry.getValue().stream()
                                .sorted(Comparator.comparing(DailyTodoCertification::getCreatedAt).reversed())
                                .map(this::certificationInfo)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    private GetMemberAllStatsApiResponseV0.DailyTodoCertificationInfo certificationInfo(DailyTodoCertification certification) {
        DailyTodo todo = certification.getDailyTodo();

        return new GetMemberAllStatsApiResponseV0.DailyTodoCertificationInfo(
                todo.getId(),
                todo.getContent(),
                certification.getReviewStatus().name(),
                certification.getContent(),
                certification.getMediaUrl(),
                certification.findReviewFeedback().orElse(null)
        );
    }

    public FindMyProfileDto getMyProfile(final Long memberId) {
        final Member member = getMember(memberId);

        return new FindMyProfileDto(
                member.getName(),
                member.getProfileImageUrl()
        );
    }
}
