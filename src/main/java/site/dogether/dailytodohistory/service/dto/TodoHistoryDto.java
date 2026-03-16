package site.dogether.dailytodohistory.service.dto;

public record TodoHistoryDto(
    Long id,
    Long todoId,
    String content,
    String status,
    boolean canRequestCertification,
    boolean canRequestCertificationReview,
    String certificationContent,
    String certificationMediaUrl,
    boolean isRead,
    String reviewFeedback
) {}
