package site.dogether.dailytodocertification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.dto.response.ApiResponse;
import site.dogether.dailytodocertification.controller.request.CertifyDailyTodoRequest;
import site.dogether.dailytodocertification.controller.request.ReviewDailyTodoCertificationRequest;
import site.dogether.dailytodocertification.controller.response.GetDailyTodoCertificationsForReviewResponse;
import site.dogether.dailytodocertification.service.DailyTodoCertificationService;
import site.dogether.dailytodocertification.service.dto.DailyTodoCertificationDto;

import java.util.List;

import static site.dogether.common.controller.dto.response.ApiResponse.success;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class DailyTodoCertificationController {

    private final DailyTodoCertificationService dailyTodoCertificationService;

    @PostMapping("/todos/{todoId}/certify")
    public ResponseEntity<ApiResponse<Void>> certifyDailyTodo(
        @Authenticated Long memberId,
        @PathVariable final Long todoId,
        @RequestBody final CertifyDailyTodoRequest request
    ) {
        dailyTodoCertificationService.certifyDailyTodo(memberId, todoId, request.content(), request.mediaUrl());
        return ResponseEntity.ok(success());
    }

    @PostMapping("/todo-certifications/{todoCertificationId}/review")
    public ResponseEntity<ApiResponse<Void>> reviewDailyTodoCertification(
        @Authenticated Long memberId,
        @PathVariable Long todoCertificationId,
        @RequestBody final ReviewDailyTodoCertificationRequest request
    ) {
        dailyTodoCertificationService.reviewDailyTodoCertification(
            memberId,
            todoCertificationId,
            request.result(),
            request.reviewFeedback()
        );
        return ResponseEntity.ok(success());
    }

    @GetMapping("/todo-certifications/pending-review")
    public ResponseEntity<ApiResponse<GetDailyTodoCertificationsForReviewResponse>> getDailyTodoCertificationsForReview(
        @Authenticated Long memberId
    ) {
        final List<DailyTodoCertificationDto> todoCertificationsForReview = dailyTodoCertificationService.findAllTodoCertificationsToReviewer(memberId);
        final GetDailyTodoCertificationsForReviewResponse response = GetDailyTodoCertificationsForReviewResponse.from(todoCertificationsForReview);

        return ResponseEntity.ok(success(response));
    }
}
