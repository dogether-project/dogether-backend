package site.dogether.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.dogether.auth.controller.request.LoginRequest;
import site.dogether.auth.controller.request.WithdrawRequest;
import site.dogether.auth.controller.response.LoginResponse;
import site.dogether.auth.service.JwtHandler;
import site.dogether.member.domain.Member;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;
import site.dogether.member.infrastructure.repository.MemberJpaRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberJpaRepository memberJpaRepository;
    private final JwtHandler jwtHandler;

    public LoginResponse login(LoginRequest request) {
        Member member = new Member(
                request.idToken(),
                request.name()
        );
        MemberJpaEntity memberJpaEntity = new MemberJpaEntity(
                member.getProviderId(),
                member.getName()
        );
        member = memberJpaRepository.save(memberJpaEntity).toDomain();

        String token = jwtHandler.createToken(member.getId());
        return new LoginResponse(member.getName(), token);
    }

    public void withdraw(String token, WithdrawRequest request) {
        token = token.substring("Bearer ".length());
        Long memberId = jwtHandler.getMemberId(token);

        MemberJpaEntity memberJpaEntity = memberJpaRepository.findById(memberId).get();
        memberJpaRepository.delete(memberJpaEntity);
    }

    public Member findMemberByToken(String token) {
        token = token.substring("Bearer ".length());
        Long memberId = jwtHandler.getMemberId(token);
        MemberJpaEntity memberJpaEntity = memberJpaRepository.findById(memberId).get();
        return memberJpaEntity.toDomain();
    }

}
