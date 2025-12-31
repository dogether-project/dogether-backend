package site.dogether.dailytodo.service.dto;

import lombok.RequiredArgsConstructor;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;

import java.util.Optional;

@RequiredArgsConstructor
public class DailyTodoDto {

    private final Long id;
    private final String content;
    private final String status;
    private final boolean canRequestCertificationReview;
    private final String certificationContent;
    private final String certificationMediaUrl;
    private final String reviewFeedback;

    public DailyTodoDto(final DailyTodo dailyTodo) {
        this(
            dailyTodo.getId(),
            dailyTodo.getContent(),
            dailyTodo.getStatus().name(),
            false,
            null,
            null,
            null
        );
    }

    public DailyTodoDto(final DailyTodo dailyTodo, final DailyTodoCertification dailyTodoCertification, final boolean canRequestCertificationReview) {
        this(
            dailyTodo.getId(),
            dailyTodo.getContent(),
            dailyTodoCertification.getReviewStatus().name(),
            canRequestCertificationReview,
            dailyTodoCertification.getContent(),
            dailyTodoCertification.getMediaUrl(),
            dailyTodoCertification.findReviewFeedback().orElse(null)
        );
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }

    public boolean canRequestCertificationReview() {
        return canRequestCertificationReview;
    }

    public Optional<String> findCertificationContent() {
        return Optional.ofNullable(certificationContent);
    }

    public Optional<String> findCertificationMediaUrl() {
        return Optional.ofNullable(certificationMediaUrl);
    }

    public Optional<String> findReviewFeedback() {
        return Optional.ofNullable(reviewFeedback);
    }
}
