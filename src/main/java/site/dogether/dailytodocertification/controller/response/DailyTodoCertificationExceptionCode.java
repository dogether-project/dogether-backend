package site.dogether.dailytodocertification.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum DailyTodoCertificationExceptionCode implements ExceptionCode {

    INVALID_DAILY_TODO_CERTIFICATION("TCF-0001")
    ;

    private final String value;
}
