package site.dogether.docs.reminder.v1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import site.dogether.docs.util.RestDocsSupport;
import site.dogether.reminder.controller.v1.TodoActivityReminderControllerV1;
import site.dogether.reminder.controller.v1.dto.request.SendTodoActivityReminderApiRequestV1;
import site.dogether.reminder.service.TodoActivityReminderService;

import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static site.dogether.reminder.entity.DailyTodoActivityReminderType.TODO_CERTIFICATION;

@DisplayName("투두 활동 재촉 V1 API 문서화 테스트")
public class TodoActivityReminderControllerV1DocsTest extends RestDocsSupport {

    private final TodoActivityReminderService todoActivityReminderService = mock(TodoActivityReminderService.class);

    @Override
    protected Object initController() {
        return new TodoActivityReminderControllerV1(todoActivityReminderService);
    }

    @DisplayName("[V1] 투두 활동 재촉 알림 전송 API")
    @Test
    void sendTodoActivityReminderV1() throws Exception {
        final SendTodoActivityReminderApiRequestV1 request = new SendTodoActivityReminderApiRequestV1(TODO_CERTIFICATION);

        mockMvc.perform(
            post("/api/v1/todos/{todoId}/reminders", 1)
                .header("Authorization", "Bearer access_token")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                pathParameters(
                    parameterWithName("todoId")
                        .description("재촉할 투두 id")
                        .attributes(constraints("유효한 데일리 투두 id만 입력 가능"), pathVariableExample(1))),
                requestFields(
                    fieldWithPath("reminderType")
                        .description("투두 활동 재촉 유형")
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("옵션으로 정해진 값만 허용"))
                        .attributes(options("TODO_CERTIFICATION(투두 인증)", "TODO_CERTIFICATION_REVIEW(투두 인증 검사)"))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING))));
    }
}
