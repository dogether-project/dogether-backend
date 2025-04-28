package site.dogether.member.service;

import java.util.Optional;
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
    public Member save(final String providerId, final String name) {
        Optional<Member> found = memberRepository.findByProviderId(providerId);

        if (found.isPresent()) {
            Member member = found.get();
            if (!member.isDeleted()) {
                return member;
            }
            hardDelete(member);
            return createMember(providerId, name);
        }
        return createMember(providerId, name);
    }

    private void hardDelete(Member member) {
        memberRepository.delete(member);
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
