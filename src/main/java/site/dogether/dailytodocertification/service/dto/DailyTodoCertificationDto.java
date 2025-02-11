package site.dogether.dailytodocertification.service.dto;

import site.dogether.dailytodocertification.domain.DailyTodoCertification;

import java.util.List;

public record DailyTodoCertificationDto(
    Long id,
    String content,
    List<String> mediaUrls,
    String todoContent
) {
    public static DailyTodoCertificationDto from(final DailyTodoCertification dailyTodoCertification, final List<String> mediaUrls) {
        return new DailyTodoCertificationDto(
            dailyTodoCertification.getId(),
            dailyTodoCertification.getContent(),
            mediaUrls,
            dailyTodoCertification.getDailyTodoContent()
        );
    }
}
