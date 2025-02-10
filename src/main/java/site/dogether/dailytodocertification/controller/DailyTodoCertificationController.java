package site.dogether.dailytodocertification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.dogether.common.config.web.resolver.Authentication;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.dailytodocertification.controller.request.ReviewDailyTodoCertificationRequest;
import site.dogether.dailytodocertification.controller.response.GetDailyTodoCertificationByIdResponse;
import site.dogether.dailytodocertification.controller.response.GetDailyTodoCertificationsForReviewResponse;
import site.dogether.dailytodocertification.controller.response.DailyTodoCertificationResponse;
import site.dogether.dailytodocertification.service.DailyTodoCertificationService;

import java.util.List;

import static site.dogether.dailytodocertification.controller.response.DailyTodoCertificationSuccessCode.*;

@RequiredArgsConstructor
@RequestMapping("/api/todo-certifications")
@RestController
public class DailyTodoCertificationController {

    private final DailyTodoCertificationService dailyTodoCertificationService;

    @PostMapping("{todoCertificationId}/review")
    public ResponseEntity<ApiResponse<Void>> reviewDailyTodoCertification(
        @Authentication String authenticationToken,
        @PathVariable Long todoCertificationId,
        @RequestBody final ReviewDailyTodoCertificationRequest request
    ) {
        dailyTodoCertificationService.reviewDailyTodoCertification(
            authenticationToken,
            todoCertificationId,
            request.result(),
            request.rejectReason()
        );
        return ResponseEntity.ok(ApiResponse.success(REVIEW_DAILY_TODO_CERTIFICATION));
    }

    @GetMapping("/pending-review")
    public ResponseEntity<ApiResponse<GetDailyTodoCertificationsForReviewResponse>> getDailyTodoCertificationsForReview() {
        return ResponseEntity.ok(ApiResponse.successWithData(
            GET_DAILY_TODO_CERTIFICATIONS_FOR_REVIEW,
            new GetDailyTodoCertificationsForReviewResponse(
                List.of(
                    new DailyTodoCertificationResponse(
                        1L,
                        "이 노력, 땀 그 모든것이 내 노력의 증거입니다. 양심 있으면 인정 누르시죠.",
                        List.of(
                            "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/daily-todo-proof-media/mock/e1.png",
                            "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/daily-todo-proof-media/mock/e2.png"),
                        "유산소 & 무산소 1시간 조지기"),
                    new DailyTodoCertificationResponse(
                        2L,
                        "공부까지 갓벽...",
                        List.of(
                            "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/daily-todo-proof-media/mock/s1.png",
                            "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/daily-todo-proof-media/mock/s2.png"),
                        "공부 3시간 조지기")))));
    }

    @GetMapping("/{todoCertificationId}")
    public ResponseEntity<ApiResponse<GetDailyTodoCertificationByIdResponse>> getDailyTodoCertificationById(@PathVariable Long todoCertificationId) {
        return ResponseEntity.ok(ApiResponse.successWithData(
            GET_DAILY_TODO_CERTIFICATION_BY_ID,
            new GetDailyTodoCertificationByIdResponse(
                new DailyTodoCertificationResponse(
                    1L,
                    "이 노력, 땀 그 모든것이 내 노력의 증거입니다. 양심 있으면 인정 누르시죠.",
                    List.of(
                        "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/daily-todo-proof-media/mock/e1.png",
                        "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/daily-todo-proof-media/mock/e2.png"),
                    "유산소 & 무산소 1시간 조지기"))));
    }
}
