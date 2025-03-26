package site.dogether.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.member.domain.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;
import site.dogether.member.infrastructure.repository.MemberJpaRepository;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

    private final MemberJpaRepository memberJpaRepository;

    @Transactional
    public Member save(final Member member) {
        return memberJpaRepository.findByProviderId(member.getProviderId())
                .orElseGet(() -> {
                    MemberJpaEntity memberJpaEntity = new MemberJpaEntity(member);
                    return memberJpaRepository.save(memberJpaEntity);
                })
            .toDomain();
    }

    @Transactional
    public void delete(final Long memberId) {
        final MemberJpaEntity memberJpaEntity = memberJpaRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원입니다. (%d)", memberId)));
        memberJpaRepository.delete(memberJpaEntity);
    }

    public MemberJpaEntity getMemberEntityById(final Long memberId) {
        return memberJpaRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원입니다. (%d)", memberId)));
    }
}
