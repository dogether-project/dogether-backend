package site.dogether.memberactivity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.service.ChallengeGroupPolicy;
import site.dogether.challengegroup.service.ChallengeGroupReader;
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
import site.dogether.memberactivity.entity.DailyTodoStats;
import site.dogether.memberactivity.repository.DailyTodoStatsRepository;
import site.dogether.memberactivity.service.dto.CertificationPeriodDto;
import site.dogether.memberactivity.service.dto.CertificationsGroupedByCertificatedAtDto;
import site.dogether.memberactivity.service.dto.CertificationsGroupedByGroupCreatedAtDto;
import site.dogether.memberactivity.service.dto.ChallengeGroupInfoDto;
import site.dogether.memberactivity.service.dto.DailyTodoCertificationInfoDto;
import site.dogether.memberactivity.service.dto.FindMyProfileDto;
import site.dogether.memberactivity.service.dto.MyCertificationStatsDto;
import site.dogether.memberactivity.service.dto.MyCertificationStatsInChallengeGroupDto;
import site.dogether.memberactivity.service.dto.MyRankInChallengeGroupDto;

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

    private final ChallengeGroupReader challengeGroupReader;
    private final ChallengeGroupMemberRepository challengeGroupMemberRepository;
    private final DailyTodoRepository dailyTodoRepository;
    private final DailyTodoCertificationRepository dailyTodoCertificationRepository;
    private final DailyTodoStatsRepository dailyTodoStatsRepository;
    private final MemberRepository memberRepository;
    private final ChallengeGroupService challengeGroupService;
    private final ChallengeGroupPolicy challengeGroupPolicy;

    private static final int CERTIFICATION_PERIOD_DAYS = 4;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public void initDailyTodoStats(final Member member) {
        final DailyTodoStats stats = new DailyTodoStats(member);
        dailyTodoStatsRepository.save(stats);
    }

    public ChallengeGroupInfoDto getChallengeGroupInfo(final Long memberId, final Long groupId) {
        final Member member = getMember(memberId);
        final ChallengeGroup challengeGroup = challengeGroupReader.getById(groupId);

        challengeGroupPolicy.validateChallengeGroupNotFinished(challengeGroup);
        challengeGroupPolicy.validateMemberIsInChallengeGroup(challengeGroup, member);

        final int currentMemberCount = challengeGroupMemberRepository.countByChallengeGroup(challengeGroup);

        final String endAt = challengeGroup.getEndAt()
                .format(DateTimeFormatter.ofPattern("yy.MM.dd"));

        return new ChallengeGroupInfoDto(
                challengeGroup.getName(),
                challengeGroup.getMaximumMemberCount(),
                currentMemberCount,
                challengeGroup.getJoinCode().getValue(),
                endAt
        );
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 id입니다. (%d)", memberId)));
    }

    public List<CertificationPeriodDto> getCertificationPeriods(final Long memberId, final Long groupId) {
        final Member member = getMember(memberId);
        final ChallengeGroup challengeGroup = challengeGroupReader.getById(groupId);

        challengeGroupPolicy.validateChallengeGroupNotFinished(challengeGroup);
        challengeGroupPolicy.validateMemberIsInChallengeGroup(challengeGroup, member);

        final List<CertificationPeriodDto> result = new ArrayList<>();
        final LocalDate today = LocalDate.now();
        final LocalDate groupStartedAt = challengeGroup.getStartAt();

        for (int i = CERTIFICATION_PERIOD_DAYS - 1; i >= 0; i--) {
            final LocalDate targetDate = today.minusDays(i);

            if (targetDate.isBefore(groupStartedAt) || targetDate.isAfter(challengeGroup.getEndAt())) {
                continue;
            }

            final int day = (int) ChronoUnit.DAYS.between(groupStartedAt, targetDate) + 1;

            result.add(getCertificationPeriodInfo(
                day,
                targetDate,
                member,
                challengeGroup
            ));
        }

        return result;
    }

    private CertificationPeriodDto getCertificationPeriodInfo(
        final int day,
        final LocalDate date,
        final Member member,
        final ChallengeGroup challengeGroup
    ) {
        final LocalDateTime startOfDay = date.atStartOfDay();
        final LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        final List<DailyTodo> todos = dailyTodoRepository.findAllByChallengeGroupAndMemberAndWrittenAtBetween(
                challengeGroup,
                member,
                startOfDay,
                endOfDay
        );

        final int createdCount = todos.size();
        final int certificatedCount = calculateCertificatedCount(todos);
        final int certificationRate = calculateCertificationRate(createdCount, certificatedCount);

        return new CertificationPeriodDto(
                day,
                createdCount,
                certificatedCount,
                certificationRate
        );
    }

    private int calculateCertificatedCount(final List<DailyTodo> todos) {
        return (int) todos.stream()
            .filter(DailyTodo::isCertifyCompleted)
            .count();
    }

    private int calculateCertificationRate(final int createdCount, final int certificatedCount) {
        if (createdCount == 0) {
            return 0;
        }
        return (int) (((double) certificatedCount / createdCount) * 100);
    }

    public MyRankInChallengeGroupDto getMyRankInChallengeGroup(final Long memberId, final Long groupId) {
        final Member target = getMember(memberId);
        final ChallengeGroup challengeGroup = challengeGroupReader.getById(groupId);

        challengeGroupPolicy.validateChallengeGroupNotFinished(challengeGroup);
        challengeGroupPolicy.validateMemberIsInChallengeGroup(challengeGroup, target);

        final List<ChallengeGroupMember> groupMembers = challengeGroupMemberRepository.findAllByChallengeGroup(challengeGroup);

        final int totalMemberCount = challengeGroupMemberRepository.countByChallengeGroup(challengeGroup);
        final int myRank = challengeGroupService.getMyRank(target, groupMembers);

        return new MyRankInChallengeGroupDto(totalMemberCount, myRank);
    }

    public MyCertificationStatsInChallengeGroupDto getMyCertificationStatsInChallengeGroup(final Long memberId, final Long groupId) {
        final Member member = getMember(memberId);
        final ChallengeGroup challengeGroup = challengeGroupReader.getById(groupId);

        challengeGroupPolicy.validateChallengeGroupNotFinished(challengeGroup);
        challengeGroupPolicy.validateMemberIsInChallengeGroup(challengeGroup, member);

        final DailyTodoCertificationCount dailyTodoCertificationCount = dailyTodoCertificationRepository.countDailyTodoCertification(challengeGroup, member);

        return new MyCertificationStatsInChallengeGroupDto(
            dailyTodoCertificationCount.getTotalCount(),
            dailyTodoCertificationCount.getApprovedCount(),
            dailyTodoCertificationCount.getRejectedCount()
        );
    }

    public MyCertificationStatsDto getMyCertificationStats(final Long memberId) {
        final Member member = getMember(memberId);

        return dailyTodoStatsRepository.findByMember(member)
            .map(stats -> new MyCertificationStatsDto(
                stats.getCertificatedCount(),
                stats.getApprovedCount(),
                stats.getRejectedCount()
            ))
            .orElseGet(() -> new MyCertificationStatsDto(0, 0, 0));
    }

    public Slice<DailyTodoCertification> getCertificationsByStatus(final Long memberId, final String status, final Pageable pageable) {
        final Member member = getMember(memberId);

        if (status != null && !status.isBlank()) {
            final DailyTodoCertificationReviewStatus dailyTodoCertificationReviewStatus = DailyTodoCertificationReviewStatus.convertByValue(status);
            return dailyTodoCertificationRepository.findAllByDailyTodo_MemberAndReviewStatusOrderByCreatedAtDesc(member, dailyTodoCertificationReviewStatus, pageable);
        }

        return dailyTodoCertificationRepository.findAllByDailyTodo_MemberOrderByCreatedAtDesc(member, pageable);
    }

    public List<CertificationsGroupedByCertificatedAtDto> certificationsGroupedByCertificatedAt(final List<DailyTodoCertification> certifications) {
        return certifications.stream()
            .collect(Collectors.groupingBy(certification -> certification.getCreatedAt().toLocalDate().format(DATE_FORMATTER)))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
            .map(entry -> new CertificationsGroupedByCertificatedAtDto(
                entry.getKey(),
                entry.getValue().stream()
                    .sorted(Comparator.comparing(DailyTodoCertification::getCreatedAt).reversed())
                    .map(this::certificationInfo)
                    .collect(Collectors.toList())
            ))
            .collect(Collectors.toList());
    }

    private DailyTodoCertificationInfoDto certificationInfo(final DailyTodoCertification certification) {
        final DailyTodo todo = certification.getDailyTodo();

        return new DailyTodoCertificationInfoDto(
            todo.getId(),
            todo.getContent(),
            certification.getReviewStatus().name(),
            certification.getContent(),
            certification.getMediaUrl(),
            certification.findReviewFeedback().orElse(null)
        );
    }

    public List<CertificationsGroupedByGroupCreatedAtDto> certificationsGroupedByGroupCreatedAt(final List<DailyTodoCertification> certifications) {
        return certifications.stream()
            .collect(Collectors.groupingBy(certification -> certification.getDailyTodo().getChallengeGroup()))
            .entrySet().stream()
            .sorted(Comparator.comparing(entry -> entry.getKey().getCreatedAt(), Comparator.reverseOrder()))
            .map(entry -> new CertificationsGroupedByGroupCreatedAtDto(
                entry.getKey().getName(),
                entry.getValue().stream()
                    .sorted(Comparator.comparing(DailyTodoCertification::getCreatedAt).reversed())
                    .map(this::certificationInfo)
                    .collect(Collectors.toList())
            ))
            .collect(Collectors.toList());
    }

    public FindMyProfileDto getMyProfile(final Long memberId) {
        final Member member = getMember(memberId);

        return new FindMyProfileDto(
                member.getName(),
                member.getProfileImageUrl()
        );
    }
}
