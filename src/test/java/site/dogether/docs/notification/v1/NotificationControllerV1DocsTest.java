package site.dogether.docs.notification.v1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import site.dogether.docs.util.RestDocsSupport;
import site.dogether.notification.controller.v1.NotificationControllerV1;
import site.dogether.notification.controller.v1.dto.request.DeleteNotificationTokenApiRequestV1;
import site.dogether.notification.controller.v1.dto.request.SaveNotificationTokenApiRequestV1;
import site.dogether.notification.service.NotificationService;

import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("푸시 알림 V1 API 문서화 테스트")
public class NotificationControllerV1DocsTest extends RestDocsSupport {

    private final NotificationService notificationService = mock(NotificationService.class);

    @Override
    protected Object initController() {
        return new NotificationControllerV1(notificationService);
    }

    @DisplayName("[V1] 푸시 알림 토큰 저장 API")
    @Test
    void saveNotificationTokenV1() throws Exception {
        final SaveNotificationTokenApiRequestV1 request = new SaveNotificationTokenApiRequestV1("kelly-token-value");

        mockMvc.perform(
                post("/api/v1/notification/tokens")
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                requestFields(
                    fieldWithPath("token")
                        .description("저장할 푸시 알림 토큰")
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("FCM에서 발급받은 토큰만 입력 가능"))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING))));
    }

    @DisplayName("[V1] 푸시 알림 토큰 삭제 API")
    @Test
    void deleteNotificationTokenV1() throws Exception {
        final DeleteNotificationTokenApiRequestV1 request = new DeleteNotificationTokenApiRequestV1("kelly-token-value");

        mockMvc.perform(
                delete("/api/v1/notification/tokens")
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                requestFields(
                    fieldWithPath("token")
                        .description("삭제할 푸시 알림 토큰")
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("서버에 저장된 FCM 토큰 입력"))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING))));
    }
}
