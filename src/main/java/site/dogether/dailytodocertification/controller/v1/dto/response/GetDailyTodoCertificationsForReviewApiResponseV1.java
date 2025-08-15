package site.dogether.dailytodocertification.controller.v1.dto.response;

import site.dogether.dailytodocertification.service.dto.DailyTodoCertificationDto;

import java.util.List;

public record GetDailyTodoCertificationsForReviewApiResponseV1(List<Data> dailyTodoCertifications) {

    public static GetDailyTodoCertificationsForReviewApiResponseV1 from(final List<DailyTodoCertificationDto> dailyTodoCertificationDtos) {
        final List<Data> responseData = dailyTodoCertificationDtos.stream()
            .map(dto -> new Data(
                dto.id(),
                dto.content(),
                dto.mediaUrl(),
                dto.todoContent(),
                dto.doer()))
            .toList();

        return new GetDailyTodoCertificationsForReviewApiResponseV1(responseData);
    }

    record Data(
        Long id,
        String content,
        String mediaUrl,
        String todoContent,
        String doer
    ) {}
}
