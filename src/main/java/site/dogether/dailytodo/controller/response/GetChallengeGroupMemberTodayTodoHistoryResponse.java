package site.dogether.dailytodo.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodohistory.service.dto.FindTargetMemberTodayTodoHistoriesDto;
import site.dogether.dailytodohistory.service.dto.TodoHistoryDto;

import java.util.List;

public record GetChallengeGroupMemberTodayTodoHistoryResponse(
    int currentTodoHistoryToReadIndex,
    List<TodoData> todos
) {
    public static GetChallengeGroupMemberTodayTodoHistoryResponse from(final FindTargetMemberTodayTodoHistoriesDto dto) {
        final List<TodoData> todos = dto.todoHistories().stream().map(TodoData::from).toList();
        return new GetChallengeGroupMemberTodayTodoHistoryResponse(dto.currentTodoHistoryToReadIndex(), todos);
    }

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
        public static TodoData from(final TodoHistoryDto dto) {
            return new TodoData(
                dto.id(),
                dto.content(),
                dto.status(),
                dto.certificationContent(),
                dto.certificationMediaUrl(),
                dto.isRead()
            );
        }
    }
}
