package site.dogether.appinfo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.appinfo.controller.v1.dto.response.ForceUpdateCheckApiResponseV1;
import site.dogether.appinfo.service.AppInfoService;
import site.dogether.common.controller.dto.response.ApiResponse;

import static site.dogether.common.controller.dto.response.ApiResponse.success;

//TODO: 향후 클라이언트에서 V1 api를 도입할 경우 해당 controller는 제거

@RequiredArgsConstructor
@RestController
public class AppInfoController {

    private final AppInfoService appInfoService;

    @GetMapping("/api/app-info/force-update-check")
    public ResponseEntity<ApiResponse<ForceUpdateCheckApiResponseV1>> forceUpdateCheck(@RequestParam("app-version") String appVersion) {
        final ForceUpdateCheckApiResponseV1 response = new ForceUpdateCheckApiResponseV1(appInfoService.forceUpdateCheck(appVersion));
        return ResponseEntity.ok(success(response));
    }
}
