package site.dogether.auth.controller.v1;

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
import site.dogether.auth.service.dto.response.LoginResponseDto;
import site.dogether.common.controller.dto.response.ApiResponse;

import static site.dogether.common.controller.dto.response.ApiResponse.success;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthControllerV1 {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginApiResponseV1>> login(
            @RequestBody final LoginApiRequestV1 request
    ) {
        final LoginResponseDto responseDto = authService.login(request.toLoginRequestDto());
        return ResponseEntity.ok(success(new LoginApiResponseV1(responseDto)));
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
