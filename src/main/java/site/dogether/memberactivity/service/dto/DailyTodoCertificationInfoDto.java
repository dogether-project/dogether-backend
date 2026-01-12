package site.dogether.memberactivity.service.dto;

public record DailyTodoCertificationInfoDto(
    Long id,
    String content,
    String status,
    String certificationContent,
    String certificationMediaUrl,
    String reviewFeedback
) {
}
