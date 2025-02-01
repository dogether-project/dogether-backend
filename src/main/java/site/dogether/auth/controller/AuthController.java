package site.dogether.auth.controller;

import static site.dogether.auth.controller.response.AuthSuccessCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.controller.request.LoginRequest;
import site.dogether.auth.controller.request.WithdrawRequest;
import site.dogether.auth.controller.response.LoginResponse;
import site.dogether.common.controller.response.ApiResponse;

@RequestMapping("/api/auth")
@RestController()
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody final LoginRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.successWithData(
            LOGIN,
            new LoginResponse("김영재", "accessTokenaccessTokenaccessToken")
        ));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @RequestBody final WithdrawRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(WITHDRAW));
    }

}
