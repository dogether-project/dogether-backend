package site.dogether.dailytodohistory.service.dto;

import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodohistory.entity.DailyTodoHistory;

public record TodoHistoryDto(
    Long id,
    String content,
    DailyTodoStatus status,
    String certificationContent,
    String certificationMediaUrl,
    boolean isRead
) {
    public static TodoHistoryDto fromTodoHistory(final DailyTodoHistory dailyTodoHistory, final boolean isHistoryRead) {
        return new TodoHistoryDto(
            dailyTodoHistory.getId(),
            dailyTodoHistory.getTodoContent(),
            dailyTodoHistory.getTodoStatus(),
            dailyTodoHistory.getTodoCertificationContent(),
            dailyTodoHistory.getTodoCertificationMediaUrl(),
            isHistoryRead
        );
    }
}
