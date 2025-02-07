package site.dogether.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.dogether.common.config.web.resolver.Authentication;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.notification.controller.request.DeleteNotificationTokenRequest;
import site.dogether.notification.controller.request.SaveNotificationTokenRequest;
import site.dogether.notification.service.NotificationService;

import static site.dogether.notification.controller.response.NotificationSuccessCode.DELETE_NOTIFICATION_TOKEN;
import static site.dogether.notification.controller.response.NotificationSuccessCode.SAVE_NOTIFICATION_TOKEN;

@RequiredArgsConstructor
@RequestMapping("/api/notification")
@RestController
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/tokens")
    public ResponseEntity<ApiResponse<Void>> saveNotificationToken(
            @Authentication final String authenticationToken,
            @RequestBody final SaveNotificationTokenRequest request) {
        notificationService.saveNotificationToken(authenticationToken, request.token());
        return ResponseEntity.ok(ApiResponse.success(SAVE_NOTIFICATION_TOKEN));
    }

    @DeleteMapping("/tokens")
    public ResponseEntity<ApiResponse<Void>> deleteNotificationToken(
            @Authentication final String authenticationToken,
            @RequestBody final DeleteNotificationTokenRequest request) {
        notificationService.deleteNotificationToken(authenticationToken, request.token());
        return ResponseEntity.ok(ApiResponse.success(DELETE_NOTIFICATION_TOKEN));
    }
}
