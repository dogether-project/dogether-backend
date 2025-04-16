package site.dogether.dailytodocertification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.dailytodocertification.controller.request.CertifyDailyTodoRequest;
import site.dogether.dailytodo.service.DailyTodoService;
import site.dogether.dailytodocertification.controller.request.ReviewDailyTodoCertificationRequest;
import site.dogether.dailytodocertification.controller.response.DailyTodoCertificationResponse;
import site.dogether.dailytodocertification.controller.response.GetDailyTodoCertificationByIdResponse;
import site.dogether.dailytodocertification.controller.response.GetDailyTodoCertificationsForReviewResponse;
import site.dogether.dailytodocertification.service.DailyTodoCertificationService;
import site.dogether.dailytodocertification.service.dto.DailyTodoCertificationDto;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static site.dogether.dailytodo.controller.response.DailyTodoSuccessCode.CERTIFY_DAILY_TODO;
import static site.dogether.dailytodocertification.controller.response.DailyTodoCertificationSuccessCode.*;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class DailyTodoCertificationController {

    private final DailyTodoService dailyTodoService;
    private final DailyTodoCertificationService dailyTodoCertificationService;

    @PostMapping("/todos/{todoId}/certify")
    public ResponseEntity<ApiResponse<Void>> certifyDailyTodo(
        @Authenticated Long memberId,
        @PathVariable final Long todoId,
        @RequestBody final CertifyDailyTodoRequest request
    ) {
        dailyTodoService.certifyDailyTodo(memberId, todoId, request.content(), request.mediaUrl());
        return ResponseEntity.ok(ApiResponse.success(CERTIFY_DAILY_TODO));
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
            request.rejectReason()
        );
        return ResponseEntity.ok(ApiResponse.success(REVIEW_DAILY_TODO_CERTIFICATION));
    }

    @GetMapping("/todo-certifications/pending-review")
    public ResponseEntity<ApiResponse<GetDailyTodoCertificationsForReviewResponse>> getDailyTodoCertificationsForReview(
        @Authenticated Long memberId
    ) {
        final List<DailyTodoCertificationDto> todoCertificationsForReview = dailyTodoCertificationService.findAllTodoCertificationsForReview(memberId);
        final GetDailyTodoCertificationsForReviewResponse response = todoCertificationsForReview.stream()
            .map(DailyTodoCertificationResponse::of)
            .collect(collectingAndThen(toList(), GetDailyTodoCertificationsForReviewResponse::new));

        return ResponseEntity.ok(ApiResponse.successWithData(GET_DAILY_TODO_CERTIFICATIONS_FOR_REVIEW, response));
    }

    @GetMapping("/todo-certifications/{todoCertificationId}")
    public ResponseEntity<ApiResponse<GetDailyTodoCertificationByIdResponse>> getDailyTodoCertificationById(@PathVariable Long todoCertificationId) {
        final DailyTodoCertificationDto dailyTodoCertification = dailyTodoCertificationService.findTodoCertificationById(todoCertificationId);
        final GetDailyTodoCertificationByIdResponse response = new GetDailyTodoCertificationByIdResponse(DailyTodoCertificationResponse.of(dailyTodoCertification));

        return ResponseEntity.ok(ApiResponse.successWithData(GET_DAILY_TODO_CERTIFICATION_BY_ID, response));
    }
}
