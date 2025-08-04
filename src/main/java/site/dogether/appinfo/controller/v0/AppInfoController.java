package site.dogether.appinfo.controller.v0;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.appinfo.controller.v0.dto.response.ForceUpdateCheckResponse;
import site.dogether.appinfo.service.AppInfoService;
import site.dogether.common.controller.response.ApiResponse;

import static site.dogether.common.controller.response.ApiResponse.success;

@RequiredArgsConstructor
@RestController
public class AppInfoController {

    private final AppInfoService appInfoService;

    @GetMapping("/api/app-info/force-update-check")
    public ResponseEntity<ApiResponse<ForceUpdateCheckResponse>> forceUpdateCheck(@RequestParam("app-version") String appVersion) {
        final ForceUpdateCheckResponse response = new ForceUpdateCheckResponse(appInfoService.forceUpdateCheck(appVersion));
        return ResponseEntity.ok(success(response));
    }
}
