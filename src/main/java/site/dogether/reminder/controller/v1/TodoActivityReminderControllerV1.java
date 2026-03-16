package site.dogether.reminder.controller.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.dto.response.ApiResponse;
import site.dogether.reminder.controller.v1.dto.request.SendTodoActivityReminderApiRequestV1;
import site.dogether.reminder.service.TodoActivityReminderService;

import static site.dogether.common.controller.dto.response.ApiResponse.*;

@RequiredArgsConstructor
@RestController
public class TodoActivityReminderControllerV1 {

    private final TodoActivityReminderService todoActivityReminderService;

    @PostMapping("/api/v1/todos/{todoId}/reminders")
    public ResponseEntity<ApiResponse<Void>> sendTodoActivityReminder(
        @Authenticated final Long memberId,
        @PathVariable final Long todoId,
        @RequestBody final SendTodoActivityReminderApiRequestV1 request
    ) {
        todoActivityReminderService.sendReminder(memberId, todoId, request.reminderType());
        return ResponseEntity.ok(success());
    }
}
