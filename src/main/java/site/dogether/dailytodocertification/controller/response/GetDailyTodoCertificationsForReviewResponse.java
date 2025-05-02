package site.dogether.dailytodocertification.controller.response;

import site.dogether.dailytodocertification.service.dto.DailyTodoCertificationDto;

import java.util.List;

public record GetDailyTodoCertificationsForReviewResponse(List<Data> dailyTodoCertifications) {

    public static GetDailyTodoCertificationsForReviewResponse from(final List<DailyTodoCertificationDto> dailyTodoCertificationDtos) {
        final List<Data> responseData = dailyTodoCertificationDtos.stream()
            .map(dto -> new Data(
                dto.id(),
                dto.content(),
                dto.mediaUrl(),
                dto.todoContent(),
                dto.doer()))
            .toList();

        return new GetDailyTodoCertificationsForReviewResponse(responseData);
    }

    record Data(
        Long id,
        String content,
        String mediaUrl,
        String todoContent,
        String doer
    ) {}
}
