package site.dogether.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.common.controller.response.ApiResponse;

@RestController
public class CommonController {

    @GetMapping("/api/health-check")
    public ResponseEntity<ApiResponse<Void>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Dogether Backend Service is OK"));
    }
}
