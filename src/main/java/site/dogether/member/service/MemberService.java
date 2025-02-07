package site.dogether.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.auth.infrastructure.JwtHandler;
import site.dogether.member.domain.Member;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;
import site.dogether.member.infrastructure.repository.MemberJpaRepository;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

    private final MemberJpaRepository memberJpaRepository;
    private final JwtHandler jwtHandler;

    @Transactional
    public Member save(final Member member) {
        return memberJpaRepository.findByProviderId(member.getProviderId())
                .orElseGet(() -> {
                    MemberJpaEntity memberJpaEntity = new MemberJpaEntity(member);
                    return memberJpaRepository.save(memberJpaEntity);
                }).toDomain();
    }

    @Transactional
    public void delete(final Long memberId) {
        final MemberJpaEntity memberJpaEntity = memberJpaRepository.findById(memberId).get();
        memberJpaRepository.delete(memberJpaEntity);
    }

    public Member findMemberByAuthenticationToken(final String token) {
        final Long memberId = jwtHandler.getMemberId(token);

        final MemberJpaEntity memberJpaEntity = memberJpaRepository.findById(memberId).get();
        return memberJpaEntity.toDomain();
    }

    public MemberJpaEntity findMemberEntityByAuthenticationToken(final String token) {
        final Long memberId = jwtHandler.getMemberId(token);
        return memberJpaRepository.findById(memberId).get();
    }

}
