package site.dogether.member.service;

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
        return memberRepository.findByProviderId(providerId)
            .map(member -> {
                log.info("가입된 회원을 조회합니다. memberId: {}", member.getId());
                return member;
            })
            .orElseGet(() -> createMember(providerId, name));
    }

    private Member createMember(String providerId, String name) {
        log.info("신규 가입 회원을 저장합니다. providerId: {}", providerId);
        Member newMember = Member.create(providerId, name);
        final Member createdMember = memberRepository.save(newMember);
        memberActivityService.initDailyTodoStats(createdMember);

        return createdMember;
    }

    @Transactional
    public void delete(final Long memberId) {
        final Member member = getMember(memberId);
        memberRepository.delete(member);
    }

    public Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 정보입니다. (memberId : %d)", memberId)));
    }
}
