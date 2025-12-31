package site.dogether.dailytodo.controller.v2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import site.dogether.dailytodohistory.service.dto.FindTargetMemberTodayTodoHistoriesDto;
import site.dogether.dailytodohistory.service.dto.TodoHistoryDto;

import java.util.List;

public record GetChallengeGroupMemberTodayTodoHistoryApiResponseV2(
    int currentTodoHistoryToReadIndex,
    List<TodoData> todos
) {
    public static GetChallengeGroupMemberTodayTodoHistoryApiResponseV2 from(final FindTargetMemberTodayTodoHistoriesDto dto) {
        final List<TodoData> todos = dto.todoHistories().stream().map(TodoData::from).toList();
        return new GetChallengeGroupMemberTodayTodoHistoryApiResponseV2(dto.currentTodoHistoryToReadIndex(), todos);
    }

    public record TodoData(
        Long historyId,
        Long todoId,
        String content,
        String status,
        boolean canRequestCertification,
        boolean canRequestCertificationReview,
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
                dto.todoId(),
                dto.content(),
                dto.status(),
                dto.canRequestCertification(),
                dto.canRequestCertificationReview(),
                dto.certificationContent(),
                dto.certificationMediaUrl(),
                dto.isRead(),
                dto.reviewFeedback()
            );
        }
    }
}
