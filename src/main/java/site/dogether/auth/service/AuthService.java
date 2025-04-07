package site.dogether.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.auth.controller.request.LoginRequest;
import site.dogether.auth.controller.request.WithdrawRequest;
import site.dogether.auth.infrastructure.AppleOAuthProvider;
import site.dogether.auth.infrastructure.JwtHandler;
import site.dogether.member.domain.Member;
import site.dogether.member.service.MemberService;
import site.dogether.member.service.dto.AuthenticatedMember;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

    private final JwtHandler jwtHandler;
    private final AppleOAuthProvider appleOAuthProvider;
    private final MemberService memberService;

    @Transactional
    public AuthenticatedMember login(final LoginRequest request) {
        final String subject = appleOAuthProvider.parseSubject(request.idToken());
        log.info("subject of apple idToken 을 파싱합니다. sub: {}", subject);

        Member member = new Member(subject, request.name());
        member = memberService.save(member);
        log.info("회원을 저장 or 조회합니다. providerId: {}", member.getProviderId());

        final String authenticationToken = jwtHandler.createToken(member.getId());

        return new AuthenticatedMember(member.getName(), authenticationToken);
    }

    @Transactional
    public void withdraw(final Long memberId, final WithdrawRequest request) {
        boolean isRevoked = appleOAuthProvider.revoke(request.authorizationCode());
        if (isRevoked) {
            memberService.delete(memberId);
        }
    }
}
