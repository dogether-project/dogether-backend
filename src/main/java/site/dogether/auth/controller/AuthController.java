package site.dogether.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.controller.request.LoginRequest;
import site.dogether.auth.controller.request.WithdrawRequest;
import site.dogether.auth.controller.response.LoginResponse;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.auth.service.AuthService;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.member.service.dto.AuthenticatedMember;

import static site.dogether.common.controller.response.ApiResponse.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody final LoginRequest request
    ) {
        final AuthenticatedMember authenticatedMember = authService.login(request);
        return ResponseEntity.ok(success(new LoginResponse(authenticatedMember)));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @Authenticated final Long memberId,
            @RequestBody final WithdrawRequest request
    ) {
        authService.withdraw(memberId, request);
        return ResponseEntity.ok(success());
    }
}
