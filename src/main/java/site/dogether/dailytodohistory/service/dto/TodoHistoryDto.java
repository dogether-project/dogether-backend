package site.dogether.dailytodohistory.service.dto;

import site.dogether.dailytodo.entity.DailyTodoStatus;

public record TodoHistoryDto(
    Long id,
    String content,
    DailyTodoStatus status,
    String certificationContent,
    String certificationMediaUrl,
    boolean isRead
) {}
