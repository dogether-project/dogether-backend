package site.dogether.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.controller.v1.dto.request.LoginApiRequestV1;
import site.dogether.auth.controller.v1.dto.request.WithdrawApiRequestV1;
import site.dogether.auth.controller.v1.dto.response.LoginApiResponseV1;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.auth.service.AuthService;
import site.dogether.common.controller.dto.response.ApiResponse;
import site.dogether.member.service.dto.AuthenticatedMember;

import static site.dogether.common.controller.dto.response.ApiResponse.success;

//TODO: 향후 클라이언트에서 V1 api를 도입할 경우 해당 controller는 제거

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginApiResponseV1>> login(
        @RequestBody final LoginApiRequestV1 request
    ) {
        final AuthenticatedMember authenticatedMember = authService.login(request);
        return ResponseEntity.ok(success(new LoginApiResponseV1(authenticatedMember)));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
        @Authenticated final Long memberId,
        @RequestBody final WithdrawApiRequestV1 request
    ) {
        authService.withdraw(memberId, request);
        return ResponseEntity.ok(success());
    }
}
