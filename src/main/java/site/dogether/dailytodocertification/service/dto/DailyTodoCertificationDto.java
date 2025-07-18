package site.dogether.dailytodocertification.service.dto;

import site.dogether.dailytodocertification.entity.DailyTodoCertification;

public record DailyTodoCertificationDto(
    Long id,
    String content,
    String mediaUrl,
    String todoContent,
    String doer
) {
    public static DailyTodoCertificationDto from(final DailyTodoCertification dailyTodoCertification) {
        return new DailyTodoCertificationDto(
            dailyTodoCertification.getId(),
            dailyTodoCertification.getContent(),
            dailyTodoCertification.getMediaUrl(),
            dailyTodoCertification.getDailyTodoContent(),
            dailyTodoCertification.getDailyTodoWriterName()
        );
    }
}
