package site.dogether.docs.dailytodocertification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import site.dogether.dailytodocertification.controller.DailyTodoCertificationController;
import site.dogether.dailytodocertification.controller.request.CertifyDailyTodoRequest;
import site.dogether.dailytodocertification.controller.request.ReviewDailyTodoCertificationRequest;
import site.dogether.dailytodocertification.service.DailyTodoCertificationService;
import site.dogether.dailytodocertification.service.dto.DailyTodoCertificationDto;
import site.dogether.docs.util.RestDocsSupport;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("데일리 투두 수행 인증 API 문서화 테스트")
public class DailyTodoCertificationControllerDocsTest extends RestDocsSupport {

    private final DailyTodoCertificationService dailyTodoCertificationService = mock(DailyTodoCertificationService.class);

    @Override
    protected Object initController() {
        return new DailyTodoCertificationController(dailyTodoCertificationService);
    }

    @DisplayName("데일리 투두 수행 인증 생성 API")
    @Test
    void certifyDailyTodo() throws Exception {
        final long todoId = 1L;
        final CertifyDailyTodoRequest request = new CertifyDailyTodoRequest(
            "이 노력, 땀 그 모든것이 내 노력의 증거입니다. 양심 있으면 인정 누르시죠.",
            "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/daily-todo-proof-media/mock/e1.png"
        );

        mockMvc.perform(
                post("/api/todos/{todoId}/certify", todoId)
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                pathParameters(
                    parameterWithName("todoId")
                        .description("데일리 투두 id")
                        .attributes(constraints("존재하는 데일리 투두 id만 입력 가능"), pathVariableExample(todoId))),
                requestFields(
                    fieldWithPath("content")
                        .description("데일리 투두 인증 본문")
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("투두 수행 인증 본문은 1 ~ 40 길이의 문자열만 입력 가능")),
                    fieldWithPath("mediaUrl")
                        .description("데일리 투두 인증 미디어 리소스")
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("이미지를 올린 S3 URL 형식만 입력 가능"))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING))));
    }

    @DisplayName("데일리 투두 수행 인증 검사 API")
    @Test
    void reviewDailyTodoCertification() throws Exception {
        final long todoCertificationId = 1L;
        final ReviewDailyTodoCertificationRequest request = new ReviewDailyTodoCertificationRequest(
            "REJECT",
            "그게 정말 최선이야?"
        );

        mockMvc.perform(
                post("/api/todo-certifications/{todoCertificationId}/review", todoCertificationId)
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                pathParameters(
                    parameterWithName("todoCertificationId")
                        .description("데일리 투두 인증 id")
                        .attributes(constraints("등록된 데일리 투두 인증 id만 입력 가능"), pathVariableExample(todoCertificationId))),
                requestFields(
                    fieldWithPath("result")
                        .description("검사 결과")
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("시스템에서 제공하는 값만 입력 가능, [ APPROVE(인정), REJECT(노인정) ]")),
                    fieldWithPath("reviewFeedback")
                        .description("검사 피드백")
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("인정, 노인정 상관없이 필수로 작성. 60자 이하 문자열만 입력 가능."))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING))));
    }
    
    @DisplayName("본인이 검사해 줘야 하는 투두 수행 인증 전체 조회 API")
    @Test        
    void getDailyTodoCertificationsForReview() throws Exception {
        final List<DailyTodoCertificationDto> dailyTodoCertificationDtos = List.of(
            new DailyTodoCertificationDto(
                1L,
                "이 노력, 땀 그 모든것이 내 노력의 증거입니다. 양심 있으면 인정 누르시죠.",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/daily-todo-proof-media/mock/e1.png",
                "유산소 & 무산소 1시간 조지기",
                "승용차"
            ),
            new DailyTodoCertificationDto(
                2L,
                "공부까지 갓벽...",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/daily-todo-proof-media/mock/s1.png",
                "공부 3시간 조지기",
                "박지은"
            )
        );

        given(dailyTodoCertificationService.findAllTodoCertificationsToReviewer(any()))
            .willReturn(dailyTodoCertificationDtos);

        mockMvc.perform(
                get("/api/todo-certifications/pending-review")
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(createDocument(
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.dailyTodoCertifications")
                        .description("검사할 투두 수행 인증 리스트")
                        .type(JsonFieldType.ARRAY)
                        .optional(),
                    fieldWithPath("data.dailyTodoCertifications[].id")
                        .description("투두 수행 인증 id")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.dailyTodoCertifications[].content")
                        .description("수행 인증 본문")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.dailyTodoCertifications[].mediaUrl")
                        .description("인증에 포함된 미디어 리소스")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.dailyTodoCertifications[].todoContent")
                        .description("수행 인증한 투두 내용")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.dailyTodoCertifications[].doer")
                        .description("투두 수행자 이름"))));
    }
}
