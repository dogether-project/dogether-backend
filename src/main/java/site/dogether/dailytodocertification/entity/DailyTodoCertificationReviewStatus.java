package site.dogether.dailytodocertification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.dailytodocertification.exception.InvalidDailyTodoCertificationReviewStatusException;

@Getter
@RequiredArgsConstructor
public enum DailyTodoCertificationReviewStatus {

    REVIEW_PENDING("검사 대기"),
    APPROVE("인정"),
    REJECT("노인정")
    ;

    public static DailyTodoCertificationReviewStatus convertByValue(final String value) {
        try {
            return DailyTodoCertificationReviewStatus.valueOf(value.toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new InvalidDailyTodoCertificationReviewStatusException(String.format("존재하지 않는 데일리 투두 인증 상태입니다. (%s)", value));
        }
    }

    public static DailyTodoCertificationReviewStatus convertReviewResultStatusByValue(final String value) {
        final DailyTodoCertificationReviewStatus dailyTodoCertificationReviewStatus = convertByValue(value);
        if (dailyTodoCertificationReviewStatus == REVIEW_PENDING) {
            throw new InvalidDailyTodoCertificationReviewStatusException("데일리 투두 인증 검사 결과에 해당하는 값이 아닙니다.");
        }

        return dailyTodoCertificationReviewStatus;
    }

    private final String description;
}
