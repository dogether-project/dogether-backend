package site.dogether.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.auth.controller.request.LoginRequest;
import site.dogether.auth.controller.request.WithdrawRequest;
import site.dogether.auth.infrastructure.AppleOAuthProvider;
import site.dogether.auth.infrastructure.JwtHandler;
import site.dogether.member.domain.Member;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;
import site.dogether.member.infrastructure.repository.MemberJpaRepository;
import site.dogether.member.service.dto.AuthenticatedMember;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

    private final MemberJpaRepository memberJpaRepository;
    private final JwtHandler jwtHandler;
    private final AppleOAuthProvider appleOAuthProvider;

    @Transactional
    public AuthenticatedMember login(final LoginRequest request) {
        final String subject = appleOAuthProvider.getSubjectFromIdToken(request.idToken());

        Member member = new Member(
                request.idToken(),
                request.name()
        );
        final MemberJpaEntity memberJpaEntity = new MemberJpaEntity(member);
        member = memberJpaRepository.save(memberJpaEntity).toDomain();

        final String token = jwtHandler.createToken(member.getId());
        return new AuthenticatedMember(member.getName(), token);
    }

    @Transactional
    public void withdraw(final String token, final WithdrawRequest request) {
        final Long memberId = jwtHandler.getMemberId(token);

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
