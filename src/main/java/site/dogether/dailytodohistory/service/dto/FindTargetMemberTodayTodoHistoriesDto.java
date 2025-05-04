package site.dogether.dailytodohistory.service.dto;

import java.util.List;

public record FindTargetMemberTodayTodoHistoriesDto(int currentTodoHistoryToReadIndex, List<TodoHistoryDto> todoHistories) {
}
