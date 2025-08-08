package site.dogether.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.auth.constant.LoginType;
import site.dogether.auth.controller.v1.dto.request.WithdrawApiRequestV1;
import site.dogether.auth.oauth.AppleOAuthProvider;
import site.dogether.auth.oauth.JwtHandler;
import site.dogether.auth.service.dto.request.LoginRequestDto;
import site.dogether.auth.service.dto.response.LoginResponseDto;
import site.dogether.member.entity.Member;
import site.dogether.member.service.MemberService;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

    private final JwtHandler jwtHandler;
    private final AppleOAuthProvider appleOAuthProvider;
    private final MemberService memberService;

    @Transactional
    public LoginResponseDto login(final LoginRequestDto request) {
        log.info("로그인 요청을 받습니다. ({})", request.name());

        // TODO : 중복 코드를 없에도록 리팩토링
        if (request.loginType() == LoginType.APPLE) {
            final String subject = appleOAuthProvider.parseSubject(request.providerId());
            final Member savedMember = memberService.save(subject, request.name());
            final String authenticationToken = jwtHandler.createToken(savedMember.getId());

            return new LoginResponseDto(savedMember.getName(), authenticationToken);
        }

        final Member savedMember = memberService.save(request.providerId(), request.name());
        final String authenticationToken = jwtHandler.createToken(savedMember.getId());

        return new LoginResponseDto(savedMember.getName(), authenticationToken);
    }

    @Transactional
    public void withdraw(final Long memberId, final WithdrawApiRequestV1 request) {
        final boolean isRevoked = appleOAuthProvider.revoke(request.authorizationCode());
        if (isRevoked) {
            memberService.delete(memberId);
            log.info("회원 탈퇴 처리 완료. memberId: {}", memberId);
        }
    }
}
