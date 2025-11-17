package site.dogether.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.auth.constant.LoginType;
import site.dogether.auth.controller.v1.dto.request.WithdrawApiRequestV1;
import site.dogether.auth.exception.NeedAppleLoginRevokeException;
import site.dogether.auth.oauth.AppleOAuthProvider;
import site.dogether.auth.oauth.JwtHandler;
import site.dogether.auth.service.dto.request.LoginRequestDto;
import site.dogether.auth.service.dto.response.LoginResponseDto;
import site.dogether.member.entity.Member;
import site.dogether.member.repository.MemberRepository;
import site.dogether.member.service.MemberService;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

    private final JwtHandler jwtHandler;
    private final AppleOAuthProvider appleOAuthProvider;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Transactional
    public LoginResponseDto login(final LoginRequestDto request) {
        log.info("로그인 요청을 받습니다. ({})", request.name());

        final Member member = findMember(request.loginType(), request.providerId())
            .orElseGet(() -> register(request.loginType(), request.providerId(), request.name()));
        final String authenticationToken = jwtHandler.createToken(member.getId());

        return new LoginResponseDto(member.getName(), authenticationToken);
    }

    private Optional<Member> findMember(final LoginType loginType, final String providerId) {
        if (loginType == LoginType.APPLE) {
            final String subject = appleOAuthProvider.parseSubject(providerId);
            return memberRepository.findByProviderId(subject);
        }

        return memberRepository.findByProviderId(providerId);
    }

    private Member register(final LoginType loginType, final String providerId, final String name) {
        if (loginType == LoginType.APPLE) {
            if (name == null || name.isBlank()) {
                throw new NeedAppleLoginRevokeException("애플 로그인 리보크가 필요합니다.");
            }

            final String subject = appleOAuthProvider.parseSubject(providerId);
            return memberService.save(subject, name);
        }

        return memberService.save(providerId, name);
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
