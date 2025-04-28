package site.dogether.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodo.repository.DailyTodoRepository;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationRepository;
import site.dogether.member.entity.Member;
import site.dogether.memberactivity.repository.DailyTodoStatsRepository;
import site.dogether.notification.repository.NotificationTokenJpaRepository;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberWithdrawService {

    private final MemberService memberService;
    private final NotificationTokenJpaRepository notificationTokenJpaRepository;
    private final ChallengeGroupMemberRepository challengeGroupMemberRepository;
    private final DailyTodoRepository dailyTodoRepository;
    private final DailyTodoCertificationRepository dailyTodoCertificationRepository;
    private final DailyTodoStatsRepository dailyTodoStatsRepository;

    @Transactional
    public void delete(final Long memberId) {
        final Member member = memberService.getMember(memberId);
        member.softDelete();
        log.info("회원 컬럼 soft delete. memberId: {}", memberId);

        challengeGroupMemberRepository.findAllByMember(member)
                .forEach(BaseEntity::softDelete);
        log.info("회원의 챌린지 그룹 멤버 컬럼 soft delete");

        notificationTokenJpaRepository.findAllByMember(member)
                .forEach(BaseEntity::softDelete);
        log.info("회원의 알림 토큰 컬럼 soft delete");

        dailyTodoRepository.findAllByMember(member)
                .forEach(BaseEntity::softDelete);
        log.info("회원의 데일리 투두 컬럼 soft delete");

        dailyTodoCertificationRepository.findAllByReviewer(member)
                .forEach(BaseEntity::softDelete);
        log.info("회원의 데일리 투두 인증 컬럼 soft delete");

        dailyTodoStatsRepository.findByMember(member)
                .ifPresent(BaseEntity::softDelete);
        log.info("회원의 데일리 투두 통계 컬럼 soft delete");
    }
}
