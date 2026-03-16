package site.dogether.memberactivity.controller.v1.dto.response;

import site.dogether.memberactivity.service.dto.DailyTodoCertificationActivityDto;

import java.util.List;

public record GetMyGroupCertificationsApiResponseV1(
    List<Certification> certifications
) {

    public record Certification(
        Long id,
        String content,
        String status,
        boolean canRequestCertificationReview,
        String certificationContent,
        String certificationMediaUrl,
        String reviewFeedback
    ) {
        public static List<Certification> fromList(final List<DailyTodoCertificationActivityDto> dtoList) {
            return dtoList.stream()
                .map(Certification::from)
                .toList();
        }

        public static Certification from(final DailyTodoCertificationActivityDto dto) {
            return new Certification(
                dto.id(),
                dto.content(),
                dto.status(),
                dto.canRequestCertificationReview(),
                dto.certificationContent(),
                dto.certificationMediaUrl(),
                dto.reviewFeedback()
            );
        }
    }
}
