package site.dogether.appinfo.controller.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.appinfo.controller.v1.dto.response.ForceUpdateCheckApiResponseV1;
import site.dogether.appinfo.service.AppInfoService;
import site.dogether.common.controller.response.ApiResponse;

import static site.dogether.common.controller.response.ApiResponse.success;

@RequiredArgsConstructor
@RestController
public class AppInfoControllerV1 {

    private final AppInfoService appInfoService;

    @GetMapping("/api/v1/app-info/force-update-check")
    public ResponseEntity<ApiResponse<ForceUpdateCheckApiResponseV1>> forceUpdateCheck(@RequestParam("app-version") String appVersion) {
        final ForceUpdateCheckApiResponseV1 response = new ForceUpdateCheckApiResponseV1(appInfoService.forceUpdateCheck(appVersion));
        return ResponseEntity.ok(success(response));
    }
}
