package site.dogether.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.dto.response.ApiResponse;
import site.dogether.notification.controller.request.DeleteNotificationTokenRequest;
import site.dogether.notification.controller.request.SaveNotificationTokenRequest;
import site.dogether.notification.service.NotificationService;

import static site.dogether.common.controller.dto.response.ApiResponse.success;

@RequiredArgsConstructor
@RequestMapping("/api/notification")
@RestController
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/tokens")
    public ResponseEntity<ApiResponse<Void>> saveNotificationToken(
            @Authenticated final Long memberId,
            @RequestBody final SaveNotificationTokenRequest request) {
        notificationService.saveNotificationToken(memberId, request.token());
        return ResponseEntity.ok(success());
    }

    @DeleteMapping("/tokens")
    public ResponseEntity<ApiResponse<Void>> deleteNotificationToken(
            @Authenticated final Long memberId,
            @RequestBody final DeleteNotificationTokenRequest request) {
        notificationService.deleteNotificationToken(memberId, request.token());
        return ResponseEntity.ok(success());
    }
}
