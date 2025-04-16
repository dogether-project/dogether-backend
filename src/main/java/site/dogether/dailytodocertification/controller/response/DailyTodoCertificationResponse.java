package site.dogether.dailytodocertification.controller.response;

import site.dogether.dailytodocertification.service.dto.DailyTodoCertificationDto;

public record DailyTodoCertificationResponse(
    Long id,
    String content,
    String mediaUrl,
    String reviewer,
    String todoContent,
    String doer
) {
    public static DailyTodoCertificationResponse of(final DailyTodoCertificationDto dailyTodoCertificationDto) {
        return new DailyTodoCertificationResponse(
            dailyTodoCertificationDto.id(),
            dailyTodoCertificationDto.content(),
            dailyTodoCertificationDto.mediaUrl(),
            dailyTodoCertificationDto.reviewer(),
            dailyTodoCertificationDto.todoContent(),
            dailyTodoCertificationDto.doer()
        );
    }
}
