package site.dogether.docs.dailytodo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.dailytodo.controller.DailyTodoController;
import site.dogether.dailytodo.controller.request.CertifyDailyTodoRequest;
import site.dogether.dailytodo.controller.request.CreateDailyTodosRequest;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.service.DailyTodoService;
import site.dogether.dailytodo.service.dto.DailyTodoAndDailyTodoCertificationDto;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodocertification.entity.DailyTodoCertificationMediaUrl;
import site.dogether.docs.util.RestDocsSupport;
import site.dogether.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static site.dogether.docs.util.DocumentLinkGenerator.DocUrl.DAILY_TODO_STATUS;
import static site.dogether.docs.util.DocumentLinkGenerator.generateLink;

@DisplayName("데일리 투두 API 문서화 테스트")
public class DailyTodoControllerDocsTest extends RestDocsSupport {

    private final DailyTodoService dailyTodoService = mock(DailyTodoService.class);

    @Override
    protected Object initController() {
        return new DailyTodoController(dailyTodoService);
    }

    @DisplayName("데일리 투두 작성 API")
    @Test        
    void createDailyTodos() throws Exception {
        final CreateDailyTodosRequest request = new CreateDailyTodosRequest(List.of(
            "프로그래머스 코테 두 문제 풀기",
            "저녁 운동 조지기",
            "감정 회고록 작성하기"
        ));

        mockMvc.perform(
                post("/api/todos")
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                requestFields(
                    fieldWithPath("todos")
                        .description("데일리 투두 리스트")
                        .type(JsonFieldType.ARRAY)
                        .attributes(constraints(
                            "그룹장 외 참여한 사람이 없다면 투두 작성 불가, " +
                                "투두 개수는 2 ~ 그룹에서 정한 하루 최대 제한 개수 이하, " +
                                "투두 항목은 2 ~ 20 길이 문자열"))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING))));
    }
    
    @DisplayName("데일리 투두 수행 인증 API")
    @Test        
    void certifyDailyTodo() throws Exception {
        final long todoId = 1L;
        final CertifyDailyTodoRequest request = new CertifyDailyTodoRequest(
            "이 노력, 땀 그 모든것이 내 노력의 증거입니다. 양심 있으면 인정 누르시죠.",
            List.of(
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/daily-todo-proof-media/mock/e1.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/daily-todo-proof-media/mock/e2.png"
            )
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
                        .attributes(constraints("2 ~ 50 길이 문자열")),
                    fieldWithPath("mediaUrls")
                        .description("데일리 투두 인증 미디어 리소스")
                        .type(JsonFieldType.ARRAY)
                        .attributes(constraints("MVP에서는 이미지를 올린 S3 URL만 입력 가능"))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING))));
    }
    
    @DisplayName("어제 작성한 투두 내용 조회 API")
    @Test
    void getYesterdayDailyTodos() throws Exception {
        final List<String> yesterdayTodos = List.of(
            "치킨 먹기",
            "치즈볼 먹기",
            "뒹굴거리기",
            "승용님 괴롭히기"
        );

        given(dailyTodoService.findYesterdayDailyTodos(any()))
            .willReturn(yesterdayTodos);

        mockMvc.perform(
                get("/api/todos/my/yesterday")
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
                    fieldWithPath("data.todos")
                        .description("어제 작성한 투두 내용 리스트")
                        .optional()
                        .type(JsonFieldType.ARRAY))));
    }

    @DisplayName("내 투두 전체 조회 API")
    @Test
    void getMyDailyTodos() throws Exception {
        final Member doer = new Member(1L, "kelly-id", "kelly");
        final Member reviewer = new Member(2L, "elmo-id", "elmo");
        final ChallengeGroup challengeGroup = new ChallengeGroup(1L, "켈리와 친구들", 6, LocalDateTime.now(), LocalDateTime.now().plusDays(7), 5, "CODE", ChallengeGroupStatus.RUNNING);
        final List<DailyTodo> dailyTodos = List.of(
            new DailyTodo(1L, challengeGroup, doer, "치킨 먹기", DailyTodoStatus.CERTIFY_PENDING, null),
            new DailyTodo(2L, challengeGroup, doer, "운동 하기", DailyTodoStatus.REVIEW_PENDING, null),
            new DailyTodo(3L, challengeGroup, doer, "인강 듣기", DailyTodoStatus.APPROVE, null),
            new DailyTodo(4L, challengeGroup, doer, "DND API 구현", DailyTodoStatus.REJECT, null)
        );
        final List<DailyTodoCertification> dailyTodoCertifications = List.of(
            new DailyTodoCertification(1L, dailyTodos.get(1), reviewer, "운동 개조짐 ㅋㅋㅋㅋ"),
            new DailyTodoCertification(2L, dailyTodos.get(2), reviewer, "인강 진짜 열심히 들었습니다. ㅎ"),
            new DailyTodoCertification(3L, dailyTodos.get(3), reviewer, "API 좀 잘 만든듯 ㅋ")
        );
        final List<DailyTodoAndDailyTodoCertificationDto> dailyTodoAndDailyTodoCertificationDtos = List.of(
            DailyTodoAndDailyTodoCertificationDto.of(dailyTodos.get(0)),
            new DailyTodoAndDailyTodoCertificationDto(dailyTodos.get(1), dailyTodoCertifications.get(0), List.of(new DailyTodoCertificationMediaUrl(1L, dailyTodoCertifications.get(0), "운동 조지는 짤.png"))),
            new DailyTodoAndDailyTodoCertificationDto(dailyTodos.get(2), dailyTodoCertifications.get(1), List.of(new DailyTodoCertificationMediaUrl(2L, dailyTodoCertifications.get(1), "인강 달리는 짤.png"))),
            new DailyTodoAndDailyTodoCertificationDto(dailyTodos.get(3), dailyTodoCertifications.get(2), List.of(new DailyTodoCertificationMediaUrl(3L, dailyTodoCertifications.get(2), "API 명세짤.png"))));

        given(dailyTodoService.findMyDailyTodo(any()))
            .willReturn(dailyTodoAndDailyTodoCertificationDtos);

        mockMvc.perform(
                get("/api/todos/my")
                    .param("date", LocalDate.now().toString())
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(createDocument(
                queryParameters(
                    parameterWithName("date")
                        .description("데일리 투두 날짜")),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos")
                        .description("조회한 투두 리스트")
                        .type(JsonFieldType.ARRAY),
                    fieldWithPath("data.todos[].id")
                        .description("데일리 투두 id")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.todos[].content")
                        .description("데일리 투두 내용")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos[].status")
                        .description("데일리 투두 상태")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos[].certificationContent")
                        .description("데일리 투두 인증글 내용")
                        .optional()
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos[].certificationMediaUrl")
                        .description("데일리 투두 인증글 이미지 URL")
                        .optional()
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos[].rejectReason")
                        .description("데일리 투두 인증 노인정 사유")
                        .optional()
                        .type(JsonFieldType.STRING))));
    }

    @DisplayName("내 투두 전체 조회 - 투두 상태 필터링 API")
    @Test
    void getMyDailyTodosWithDailyTodoStatus() throws Exception {
        final Member doer = new Member(1L, "kelly-id", "kelly");
        final Member reviewer = new Member(2L, "elmo-id", "elmo");
        final ChallengeGroup challengeGroup = new ChallengeGroup(1L, "켈리와 친구들", 6, LocalDateTime.now(), LocalDateTime.now().plusDays(7), 5, "CODE", ChallengeGroupStatus.RUNNING);
        final DailyTodo dailyTodo = new DailyTodo(2L, challengeGroup, doer,  "운동 하기", DailyTodoStatus.REVIEW_PENDING, null);
        final DailyTodoCertification dailyTodoCertification = new DailyTodoCertification(1L, dailyTodo, reviewer, "운동 개조짐 ㅋㅋㅋㅋ");
        final List<DailyTodoAndDailyTodoCertificationDto> dailyTodoAndDailyTodoCertificationDtos = List.of(new DailyTodoAndDailyTodoCertificationDto(dailyTodo, dailyTodoCertification, List.of(new DailyTodoCertificationMediaUrl(1L, dailyTodoCertification, "운동 조지는 짤.png"))));

        given(dailyTodoService.findMyDailyTodo(any()))
            .willReturn(dailyTodoAndDailyTodoCertificationDtos);

        mockMvc.perform(
                get("/api/todos/my")
                    .param("date", LocalDate.now().toString())
                    .param("status", DailyTodoStatus.REVIEW_PENDING.name())
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(createDocument(
                queryParameters(
                    parameterWithName("date")
                        .description("데일리 투두 날짜"),
                    parameterWithName("status")
                        .description(generateLink(DAILY_TODO_STATUS))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos")
                        .description("조회한 투두 리스트")
                        .type(JsonFieldType.ARRAY),
                    fieldWithPath("data.todos[].id")
                        .description("데일리 투두 id")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.todos[].content")
                        .description("데일리 투두 내용")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos[].status")
                        .description("데일리 투두 상태")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos[].certificationContent")
                        .description("데일리 투두 인증글 내용")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos[].certificationMediaUrl")
                        .description("데일리 투두 인증글 이미지 URL")
                        .type(JsonFieldType.STRING))));
    }
}
