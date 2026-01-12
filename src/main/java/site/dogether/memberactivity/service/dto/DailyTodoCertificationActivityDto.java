package site.dogether.memberactivity.service.dto;

public record DailyTodoCertificationActivityDto(
    Long id,
    String content,
    String status,
    boolean canRequestCertificationReview,
    String certificationContent,
    String certificationMediaUrl,
    String reviewFeedback
) {
}
