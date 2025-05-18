package site.dogether.dailytodo.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.dailytodo.exception.InvalidDailyTodoStatusException;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DailyTodoStatus {

    CERTIFY_PENDING("인증 대기"),
    REVIEW_PENDING("검사 대기"),
    APPROVE("인정"),
    REJECT("노인정"),
    ;

    private final String description;

    public static DailyTodoStatus convertFromValue(final String value) {
        return Arrays.stream(DailyTodoStatus.values())
            .filter(status -> status.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new InvalidDailyTodoStatusException(String.format("유효하지 않은 데일리 투두 상태 값입니다. (%s)", value)));
    }

    public boolean isReviewResultStatus() {
        return this == APPROVE || this == REJECT;
    }

    public boolean isCertificatedStatus() {
        return this == APPROVE || this == REJECT || this == REVIEW_PENDING;
    }
}
