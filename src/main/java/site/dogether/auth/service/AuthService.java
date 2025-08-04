package site.dogether.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.auth.controller.v1.dto.request.LoginApiRequestV1;
import site.dogether.auth.controller.v1.dto.request.WithdrawApiRequestV1;
import site.dogether.auth.oauth.AppleOAuthProvider;
import site.dogether.auth.oauth.JwtHandler;
import site.dogether.member.entity.Member;
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
    public AuthenticatedMember login(final LoginApiRequestV1 request) {
        log.info("로그인 요청을 받습니다. ({})", request.name());

        final String subject = appleOAuthProvider.parseSubject(request.idToken());
        log.info("subject of apple idToken 을 파싱합니다. ({})", request.name());

        final Member savedMember = memberService.save(subject, request.name());
        log.info("회원을 저장 or 조회합니다. ({})", savedMember);

        final String authenticationToken = jwtHandler.createToken(savedMember.getId());

        return new AuthenticatedMember(savedMember.getName(), authenticationToken);
    }

    @Transactional
    public void withdraw(final Long memberId, final WithdrawApiRequestV1 request) {
        final boolean isRevoked = appleOAuthProvider.revoke(request.authorizationCode());
        if (isRevoked) {
            memberService.delete(memberId);
            log.info("회원 탈퇴 처리 완료. memberId: {}", memberId);
        }
    }

    @Profile("local")
    public String issueTestUserJwt(final Long testUserId) {
        final Member member = memberService.getMember(testUserId);
        return jwtHandler.createToken(member.getId());
    }
}
