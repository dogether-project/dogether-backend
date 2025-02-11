package site.dogether.dailytodocertification.controller.response;

import site.dogether.dailytodocertification.service.dto.DailyTodoCertificationDto;

import java.util.List;

public record DailyTodoCertificationResponse(
    Long id,
    String content,
    List<String> mediaUrls,
    String todoContent
) {
    public static DailyTodoCertificationResponse of(final DailyTodoCertificationDto dailyTodoCertificationDto) {
        return new DailyTodoCertificationResponse(
            dailyTodoCertificationDto.id(),
            dailyTodoCertificationDto.content(),
            dailyTodoCertificationDto.mediaUrls(),
            dailyTodoCertificationDto.todoContent()
        );
    }
}
