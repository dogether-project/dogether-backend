package site.dogether.dailytodocertification.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum DailyTodoCertificationErrorCode implements ErrorCode {

    INVALID_DAILY_TODO_CERTIFICATION("TCF-0001"),
    DAILY_TODO_CERTIFICATION_NOT_FOUND("TCF-0002"),
    NOT_DAILY_TODO_CERTIFICATION_REVIEWER("TCF-0003"),
    INVALID_DAILY_TODO_CERTIFICATION_REVIEWER("TCF-0004"),
    INVALID_DAILY_TODO_CERTIFICATION_REVIEW_STATUS("TCF-0005"),
    ;

    private final String value;
}
