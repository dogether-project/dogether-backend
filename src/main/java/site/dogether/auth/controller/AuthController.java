package site.dogether.auth.controller;

import static site.dogether.auth.controller.response.AuthSuccessCode.LOGIN;
import static site.dogether.auth.controller.response.AuthSuccessCode.WITHDRAW;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.controller.request.LoginRequest;
import site.dogether.auth.controller.request.WithdrawRequest;
import site.dogether.auth.controller.response.LoginResponse;
import site.dogether.auth.resolver.AuthToken;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.member.service.MemberService;
import site.dogether.member.service.dto.AuthenticatedMember;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody final LoginRequest request
    ) {
        final AuthenticatedMember authenticatedMember = memberService.login(request);
        final LoginResponse response = new LoginResponse(
            authenticatedMember.name(),
            authenticatedMember.accessToken()
        );
        return ResponseEntity.ok(ApiResponse.successWithData(
            LOGIN, response
        ));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthToken final String token,
            @RequestBody final WithdrawRequest request
    ) {
        memberService.withdraw(token, request);
        return ResponseEntity.ok(ApiResponse.success(WITHDRAW));
    }

}
