package site.dogether.dailytodo.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import site.dogether.dailytodo.entity.DailyTodoStatus;

import java.util.List;

public record GetChallengeMemberTodayTodosResponse(
    String memberProfileImageUrl,
    String memberName,
    int achievementRate,
    List<TodoData> todos
) {
    public record TodoData(
        Long id,
        String content,
        DailyTodoStatus status,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String certificationContent,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String certificationMediaUrl
    ) {
    }
}
