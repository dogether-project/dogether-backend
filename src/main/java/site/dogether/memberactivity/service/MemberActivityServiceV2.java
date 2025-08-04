package site.dogether.memberactivity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationRepository;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;
import site.dogether.memberactivity.controller.v2.dto.response.GetMemberAllStatsResponseV2;
import site.dogether.memberactivity.exception.InvalidParameterException;
import site.dogether.memberactivity.repository.DailyTodoStatsRepository;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//TODO: 해당 service단 V1은 추후 리팩토링 시 제거 예정

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberActivityServiceV2 {

    private final DailyTodoCertificationRepository dailyTodoCertificationRepository;
    private final DailyTodoStatsRepository dailyTodoStatsRepository;
    private final MemberRepository memberRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public GetMemberAllStatsResponseV2 getMemberAllStats(Long memberId, String sort, String status, Pageable pageable) {
        final Member member = getMember(memberId);

        GetMemberAllStatsResponseV2.DailyTodoStats stats = getStats(member);
        Slice<DailyTodoCertification> certificationsBySlice = getCertificationsByStatus(member, status, pageable);
        List<DailyTodoCertification> certifications = certificationsBySlice.getContent();

        if ("TODO_COMPLETED_AT".equals(sort)) {
            List<GetMemberAllStatsResponseV2.CertificationsGroupedByTodoCompletedAt> groupedCertifications = getCertificationsSortedByTodoCompletedAt(certifications);
            return new GetMemberAllStatsResponseV2(stats, groupedCertifications, null, GetMemberAllStatsResponseV2.from(certificationsBySlice));
        }

        if ("GROUP_CREATED_AT".equals(sort)) {
            List<GetMemberAllStatsResponseV2.CertificationsGroupedByGroupCreatedAt> groupedCertifications = getCertificationsSortedByGroupCreatedAt(certifications);
            return new GetMemberAllStatsResponseV2(stats, null, groupedCertifications, GetMemberAllStatsResponseV2.from(certificationsBySlice));
        }

        throw new InvalidParameterException("유효하지 않은 sort 파라미터입니다.");
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 id입니다. (%d)", memberId)));
    }

    private GetMemberAllStatsResponseV2.DailyTodoStats getStats(Member member) {
        return dailyTodoStatsRepository.findByMember(member)
                .map(stats -> new GetMemberAllStatsResponseV2.DailyTodoStats(
                        stats.getCertificatedCount(),
                        stats.getApprovedCount(),
                        stats.getRejectedCount()
                ))
                .orElseGet(() -> new GetMemberAllStatsResponseV2.DailyTodoStats(0, 0, 0));
    }

    private Slice<DailyTodoCertification> getCertificationsByStatus(Member member, String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            final DailyTodoCertificationReviewStatus dailyTodoCertificationReviewStatus = DailyTodoCertificationReviewStatus.convertByValue(status);
            return dailyTodoCertificationRepository.findAllByDailyTodo_MemberAndReviewStatus(member, dailyTodoCertificationReviewStatus, pageable);
        }

        return dailyTodoCertificationRepository.findAllByDailyTodo_Member(member, pageable);
    }

    private List<GetMemberAllStatsResponseV2.CertificationsGroupedByTodoCompletedAt> getCertificationsSortedByTodoCompletedAt(List<DailyTodoCertification> certifications) {
        return certifications.stream()
                .collect(Collectors.groupingBy(cert -> cert.getCreatedAt().toLocalDate().format(DATE_FORMATTER)))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .map(entry -> new GetMemberAllStatsResponseV2.CertificationsGroupedByTodoCompletedAt(
                        entry.getKey(),
                        entry.getValue().stream()
                                .sorted(Comparator.comparing(DailyTodoCertification::getCreatedAt).reversed())
                                .map(this::certificationInfo)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    private List<GetMemberAllStatsResponseV2.CertificationsGroupedByGroupCreatedAt> getCertificationsSortedByGroupCreatedAt(List<DailyTodoCertification> certifications) {
        return certifications.stream()
                .collect(Collectors.groupingBy(certification -> certification.getDailyTodo().getChallengeGroup()))
                .entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getCreatedAt(), Comparator.reverseOrder()))
                .map(entry -> new GetMemberAllStatsResponseV2.CertificationsGroupedByGroupCreatedAt(
                        entry.getKey().getName(),
                        entry.getValue().stream()
                                .sorted(Comparator.comparing(DailyTodoCertification::getCreatedAt).reversed())
                                .map(this::certificationInfo)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    private GetMemberAllStatsResponseV2.DailyTodoCertificationInfo certificationInfo(DailyTodoCertification certification) {
        DailyTodo todo = certification.getDailyTodo();

        return new GetMemberAllStatsResponseV2.DailyTodoCertificationInfo(
                todo.getId(),
                todo.getContent(),
                certification.getReviewStatus().name(),
                certification.getContent(),
                certification.getMediaUrl(),
                certification.findReviewFeedback().orElse(null)
        );
    }
}
