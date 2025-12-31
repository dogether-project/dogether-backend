package site.dogether.reminder.controller.v1.dto.request;

import site.dogether.reminder.entity.DailyTodoActivityReminderType;

public record SendTodoActivityReminderApiRequestV1(DailyTodoActivityReminderType reminderType) {
}
