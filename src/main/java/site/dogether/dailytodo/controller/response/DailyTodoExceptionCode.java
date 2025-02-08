package site.dogether.dailytodo.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum DailyTodoExceptionCode implements ExceptionCode {

    INVALID_DAILY_TODO("DTF-0001"),
    UNREVIEWED_DAILY_TODO_EXIST("DTF-0002")
    ;

    private final String value;
}
