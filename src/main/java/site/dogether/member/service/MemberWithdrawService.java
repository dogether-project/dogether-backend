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

    @Transactional
    public void delete(final Long memberId) {
        final Member member = memberService.getMember(memberId);
        member.softDelete();

        challengeGroupMemberRepository.findAllByMember(member)
                .forEach(BaseEntity::softDelete);
        notificationTokenJpaRepository.findAllByMember(member)
                .forEach(BaseEntity::softDelete);
        dailyTodoRepository.findAllByMember(member)
                .forEach(BaseEntity::softDelete);
        dailyTodoCertificationRepository.findAllByReviewer(member)
                .forEach(BaseEntity::softDelete);

        log.info("회원 탈퇴 처리 완료. memberId: {}", memberId);
    }
}
