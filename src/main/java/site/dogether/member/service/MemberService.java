package site.dogether.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member save(final Member member) {
        return memberRepository.findByProviderId(member.getProviderId())
            .orElseGet(() -> memberRepository.save(member));
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
