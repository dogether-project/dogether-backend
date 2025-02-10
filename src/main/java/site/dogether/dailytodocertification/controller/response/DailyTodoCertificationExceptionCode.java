package site.dogether.dailytodocertification.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum DailyTodoCertificationExceptionCode implements ExceptionCode {

    INVALID_DAILY_TODO_CERTIFICATION("TCF-0001"),
    DAILY_TODO_CERTIFICATION_NOT_FOUND("TCF-0002"),
    NOT_DAILY_TODO_CERTIFICATION_REVIEWER("TCF-0003"),
    ;

    private final String value;
}
