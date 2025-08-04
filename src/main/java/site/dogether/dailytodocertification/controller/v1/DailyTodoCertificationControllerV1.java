package site.dogether.dailytodocertification.controller.v1;

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
import site.dogether.dailytodocertification.controller.v1.dto.request.CertifyDailyTodoApiRequestV1;
import site.dogether.dailytodocertification.controller.v1.dto.request.ReviewDailyTodoCertificationApiRequestV1;
import site.dogether.dailytodocertification.controller.v1.dto.response.GetDailyTodoCertificationsForReviewApiResponseV1;
import site.dogether.dailytodocertification.service.DailyTodoCertificationService;
import site.dogether.dailytodocertification.service.dto.DailyTodoCertificationDto;

import java.util.List;

import static site.dogether.common.controller.dto.response.ApiResponse.success;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class DailyTodoCertificationControllerV1 {

    private final DailyTodoCertificationService dailyTodoCertificationService;

    @PostMapping("/todos/{todoId}/certify")
    public ResponseEntity<ApiResponse<Void>> certifyDailyTodo(
        @Authenticated Long memberId,
        @PathVariable final Long todoId,
        @RequestBody final CertifyDailyTodoApiRequestV1 request
    ) {
        dailyTodoCertificationService.certifyDailyTodo(memberId, todoId, request.content(), request.mediaUrl());
        return ResponseEntity.ok(success());
    }

    @PostMapping("/todo-certifications/{todoCertificationId}/review")
    public ResponseEntity<ApiResponse<Void>> reviewDailyTodoCertification(
        @Authenticated Long memberId,
        @PathVariable Long todoCertificationId,
        @RequestBody final ReviewDailyTodoCertificationApiRequestV1 request
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
    public ResponseEntity<ApiResponse<GetDailyTodoCertificationsForReviewApiResponseV1>> getDailyTodoCertificationsForReview(
        @Authenticated Long memberId
    ) {
        final List<DailyTodoCertificationDto> todoCertificationsForReview = dailyTodoCertificationService.findAllTodoCertificationsToReviewer(memberId);
        final GetDailyTodoCertificationsForReviewApiResponseV1 response = GetDailyTodoCertificationsForReviewApiResponseV1.from(todoCertificationsForReview);

        return ResponseEntity.ok(success(response));
    }
}
