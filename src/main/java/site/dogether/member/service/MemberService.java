package site.dogether.member.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;
import site.dogether.memberactivity.service.MemberActivityService;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberActivityService memberActivityService;

    @Transactional
    public Member save(final String providerId, final String name) {
        Optional<Member> found = memberRepository.findByProviderId(providerId);

        if (found.isPresent()) {
            Member member = found.get();
            if (!member.isDeleted()) {
                log.info("가입한 회원을 조회합니다. memberId: {}", member.getId());
                return member;
            }
            hardDelete(member);
            final Member createdMember = createMember(providerId, name);
            memberActivityService.initDailyTodoStats(createdMember);
            return createdMember;
        }
        log.info("신규 가입 회원을 저장합니다. providerId: {}", providerId);
        final Member createdMember = createMember(providerId, name);
        memberActivityService.initDailyTodoStats(createdMember);
        return createdMember;
    }

    private void hardDelete(Member member) {
        memberRepository.delete(member);
        memberRepository.flush();
        log.info("재가입을 위한 회원 정보 삭제. memberId: {}", member.getId());
    }

    private Member createMember(String providerId, String name) {
        Member newMember = Member.create(providerId, name);
        return memberRepository.save(newMember);
    }

    public Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 정보입니다. (memberId : %d)", memberId)));
    }
}
