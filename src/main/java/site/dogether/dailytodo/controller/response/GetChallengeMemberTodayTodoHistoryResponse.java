package site.dogether.dailytodo.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import site.dogether.dailytodo.entity.DailyTodoStatus;

import java.util.List;

public record GetChallengeMemberTodayTodoHistoryResponse(
    int currentTodoHistoryToReadIndex,
    List<TodoData> todos
) {
    public record TodoData(
        Long id,
        String content,
        DailyTodoStatus status,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String certificationContent,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String certificationMediaUrl,
        boolean isRead
    ) {
    }
}
