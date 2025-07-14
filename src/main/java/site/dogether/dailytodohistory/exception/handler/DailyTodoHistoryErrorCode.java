package site.dogether.dailytodohistory.exception.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.exception.handler.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum DailyTodoHistoryErrorCode implements ErrorCode {

    DAILY_TODO_HISTORY_ERROR("DTHF-0001", "데일리 투두 히스토리 기능에 예기치 못한 문제가 발생했습니다."),
    ;

    private final String value;
    private final String message;
}
