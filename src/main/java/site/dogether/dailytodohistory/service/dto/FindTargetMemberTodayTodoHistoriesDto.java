package site.dogether.dailytodohistory.service.dto;

import java.util.List;

public record FindTargetMemberTodayTodoHistoriesDto(boolean isMine, int currentTodoHistoryToReadIndex, List<TodoHistoryDto> todoHistories) {
}
