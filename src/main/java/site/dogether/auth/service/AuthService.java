package site.dogether.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.auth.controller.request.LoginRequest;
import site.dogether.auth.controller.request.WithdrawRequest;
import site.dogether.auth.oauth.AppleOAuthProvider;
import site.dogether.auth.oauth.JwtHandler;
import site.dogether.member.entity.Member;
import site.dogether.member.service.MemberService;
import site.dogether.member.service.MemberWithdrawService;
import site.dogether.member.service.dto.AuthenticatedMember;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

    private final JwtHandler jwtHandler;
    private final AppleOAuthProvider appleOAuthProvider;
    private final MemberService memberService;
    private final MemberWithdrawService memberWithdrawService;

    @Transactional
    public AuthenticatedMember login(final LoginRequest request) {
        log.info("로그인 요청을 받습니다. idToken: {}", request.idToken());

        final String subject = appleOAuthProvider.parseSubject(request.idToken());
        log.info("subject of apple idToken 을 파싱합니다. sub: {}", subject);

        final Member savedMember = memberService.save(subject, request.name());
        log.info("회원을 저장 or 조회합니다. providerId: {}", savedMember.getProviderId());

        final String authenticationToken = jwtHandler.createToken(savedMember.getId());

        return new AuthenticatedMember(savedMember.getName(), authenticationToken);
    }

    @Transactional
    public void withdraw(final Long memberId, final WithdrawRequest request) {
        final boolean isRevoked = appleOAuthProvider.revoke(request.authorizationCode());
        if (isRevoked) {
            memberWithdrawService.delete(memberId);
        }
    }

    @Profile("local")
    public String issueTestUserJwt(final Long testUserId) {
        final Member member = memberService.getMember(testUserId);
        return jwtHandler.createToken(member.getId());
    }
}
