package site.dogether.dailytodohistory.service.dto;

public record TodoHistoryDto(
    Long id,
    String content,
    String status,
    String certificationContent,
    String certificationMediaUrl,
    boolean isRead,
    String reviewFeedback
) {}
