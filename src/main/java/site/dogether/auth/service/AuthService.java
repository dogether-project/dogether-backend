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
        final String subject = appleOAuthProvider.getSubjectFromIdToken(request.idToken());

        Member member = new Member(
                subject,
                request.name()
        );
        member = memberService.save(member);

        final String authenticationToken = jwtHandler.createToken(member.getId());

        return new AuthenticatedMember(member.getName(), authenticationToken);
    }

    @Transactional
    public void withdraw(final String authenticationToken, final WithdrawRequest request) {
        final Long memberId = jwtHandler.getMemberId(authenticationToken);

        try {
            appleOAuthProvider.revoke(request.authorizationCode());
            memberService.delete(memberId);
        } catch (Exception e) {
            throw new RuntimeException("애플 계정 해지(revoke) 실패로 회원 탈퇴를 진행할 수 없습니다.");
        }
    }

}
