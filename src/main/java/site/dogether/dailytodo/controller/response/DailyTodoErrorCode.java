package site.dogether.dailytodo.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum DailyTodoErrorCode implements ErrorCode {

    INVALID_DAILY_TODO("DTF-0001"),
    DAILY_TODO_CREATED_DATE("DTF-0002"),
    DAILY_TODO_NOT_FOUND("DTF-0003"),
    DAILY_TODO_STATUS("DTF-0004"),
    NOT_DAILY_TODO_OWNER("DTF-0005"),
    INVALID_DAILY_TODO_STATUS("DTF-0006"),
    INVALID_REVIEW_RESULT("DTF-0007"),
    NOT_REVIEW_PENDING_DAILY_TODO("DTF-0008"),
    ;

    private final String value;
}
