package site.dogether.dailytodo.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum DailyTodoExceptionCode implements ExceptionCode {

    INVALID_DAILY_TODO("DTF-0001"),
    UNREVIEWED_DAILY_TODO_EXIST("DTF-0002"),
    DAILY_TODO_CREATED_DATE("DTF-0003"),
    DAILY_TODO_NOT_FOUND("DTF-0004"),
    DAILY_TODO_STATUS("DTF-0005"),
    NOT_DAILY_TODO_OWNER("DTF-0006")
    ;

    private final String value;
}
