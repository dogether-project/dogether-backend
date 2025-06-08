package site.dogether.dailytodo.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import site.dogether.dailytodohistory.service.dto.FindTargetMemberTodayTodoHistoriesDto;
import site.dogether.dailytodohistory.service.dto.TodoHistoryDto;

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
        String status,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String certificationContent,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String certificationMediaUrl,
        boolean isRead,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String reviewFeedback
    ) {
        public static TodoData from(final TodoHistoryDto dto) {
            return new TodoData(
                dto.id(),
                dto.content(),
                dto.status(),
                dto.certificationContent(),
                dto.certificationMediaUrl(),
                dto.isRead(),
                dto.reviewFeedback()
            );
        }
    }
}
