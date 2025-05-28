package site.dogether.dailytodohistory.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DailyTodoHistoryReadStatus {

    NULL("history 존재하지 않음"),
    READ_ALL("history 전부 읽음"),
    READ_YET("읽어야 할 history 존재"),
    ;

    private final String description;
}
