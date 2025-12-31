package site.dogether.docs.dailytodo.v2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.fixture.ChallengeGroupFixture;
import site.dogether.dailytodo.controller.v2.DailyTodoControllerV2;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.service.DailyTodoService;
import site.dogether.dailytodo.service.dto.DailyTodoDto;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodohistory.service.DailyTodoHistoryService;
import site.dogether.dailytodohistory.service.dto.FindTargetMemberTodayTodoHistoriesDto;
import site.dogether.dailytodohistory.service.dto.TodoHistoryDto;
import site.dogether.docs.util.RestDocsSupport;
import site.dogether.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static site.dogether.dailytodo.entity.DailyTodoStatus.CERTIFY_COMPLETED;
import static site.dogether.dailytodo.entity.DailyTodoStatus.CERTIFY_PENDING;
import static site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus.APPROVE;
import static site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus.REJECT;
import static site.dogether.dailytodocertification.entity.DailyTodoCertificationReviewStatus.REVIEW_PENDING;

@DisplayName("데일리 투두 V2 API 문서화 테스트")
public class DailyTodoControllerV2DocsTest extends RestDocsSupport {

    private final DailyTodoService dailyTodoService = mock(DailyTodoService.class);
    private final DailyTodoHistoryService dailyTodoHistoryService = mock(DailyTodoHistoryService.class);

    @Override
    protected Object initController() {
        return new DailyTodoControllerV2(dailyTodoService, dailyTodoHistoryService);
    }

    @DisplayName("[V2] 참여중인 특정 챌린지 그룹에서 내 데일리 투두 전체 조회 API (투두 작성 날짜만 입력)")
    @Test
    void getMyDailyTodosWithCertificationInputDateV2() throws Exception {
        final Member doer = new Member(1L, "kelly-id", "kelly", "https://영재님_얼짱_각도.png", LocalDateTime.now());
        final ChallengeGroup challengeGroup = ChallengeGroupFixture.create("켈리와 친구들", 6, LocalDate.now(), LocalDate.now().plusDays(7));
        final List<DailyTodo> dailyTodos = List.of(
            new DailyTodo(1L, challengeGroup, doer, "운동 하기", CERTIFY_COMPLETED, LocalDateTime.now()),
            new DailyTodo(2L, challengeGroup, doer, "인강 듣기", CERTIFY_COMPLETED, LocalDateTime.now()),
            new DailyTodo(3L, challengeGroup, doer, "치킨 먹기", CERTIFY_PENDING, LocalDateTime.now()),
            new DailyTodo(4L, challengeGroup, doer, "DND API 구현", CERTIFY_COMPLETED, LocalDateTime.now())
        );
        final List<DailyTodoCertification> dailyTodoCertifications = List.of(
            new DailyTodoCertification(1L, dailyTodos.get(0), "운동 개조짐 ㅋㅋㅋㅋ", "https://image.url", REVIEW_PENDING, null, LocalDateTime.now().plusHours(1)),
            new DailyTodoCertification(2L, dailyTodos.get(1), "인강 진짜 열심히 들었습니다. ㅎ", "https://image.url", APPROVE, "와.. 오늘 이걸 다 들었어요...?", LocalDateTime.now().plusHours(3)),
            new DailyTodoCertification(3L, dailyTodos.get(3), "API 좀 잘 만든듯 ㅋ", "https://image.url", REJECT, "코드 개판이네 ㅎ", LocalDateTime.now().plusHours(2))
        );

        final List<DailyTodoDto> dailyTodoDtos = List.of(
            new DailyTodoDto(dailyTodos.get(2)),
            new DailyTodoDto(dailyTodos.get(0), dailyTodoCertifications.get(0), true),
            new DailyTodoDto(dailyTodos.get(1), dailyTodoCertifications.get(1), false),
            new DailyTodoDto(dailyTodos.get(3), dailyTodoCertifications.get(2), false)
        );
        given(dailyTodoService.findMyDailyTodos(any())).willReturn(dailyTodoDtos);

        mockMvc.perform(
                get("/api/v2/challenge-groups/{groupId}/my-todos", 1)
                    .param("date", LocalDate.now().toString())
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(createDocument(
                pathParameters(
                    parameterWithName("groupId")
                        .description("챌린지 그룹 id")
                        .attributes(constraints("유효한 챌린지 그룹 id만 입력 가능"), pathVariableExample(1))),
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
                    fieldWithPath("data.todos[].canRequestCertificationReview")
                        .description("데일리 투두 인증 검사 요청 가능 여부")
                        .type(JsonFieldType.BOOLEAN),
                    fieldWithPath("data.todos[].certificationContent")
                        .description("데일리 투두 인증글 내용")
                        .optional()
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos[].certificationMediaUrl")
                        .description("데일리 투두 인증글 이미지 URL")
                        .optional()
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos[].reviewFeedback")
                        .description("데일리 투두 인증 검사 피드백")
                        .optional()
                        .type(JsonFieldType.STRING))));
    }

    @DisplayName("[V2] 참여중인 특정 챌린지 그룹에서 내 데일리 투두 전체 조회 API (투두 작성 날짜 & 투두 상태 입력)")
    @Test
    void getMyDailyTodosWithCertificationInputDateAndTodoStatusV2() throws Exception {
        final Member doer = new Member(1L, "kelly-id", "kelly", "https://영재님_얼짱_각도.png", LocalDateTime.now());
        final Member reviewer = new Member(2L, "elmo-id", "elmo", "https://영재님_얼짱_각도.png", LocalDateTime.now());
        final ChallengeGroup challengeGroup = ChallengeGroupFixture.create("켈리와 친구들", 6, LocalDate.now(), LocalDate.now().plusDays(7));
        final DailyTodo dailyTodo = new DailyTodo(2L, challengeGroup, doer,  "운동 하기", CERTIFY_COMPLETED, LocalDateTime.now().plusHours(2));
        final DailyTodoCertification dailyTodoCertification = new DailyTodoCertification(1L, dailyTodo, "운동 개조짐 ㅋㅋㅋㅋ", "https://image.url", REVIEW_PENDING, null, LocalDateTime.now().plusHours(3));
        final List<DailyTodoDto> dailyTodoDtos = List.of(new DailyTodoDto(dailyTodo, dailyTodoCertification, true));

        given(dailyTodoService.findMyDailyTodos(any())).willReturn(dailyTodoDtos);

        mockMvc.perform(
                get("/api/v2/challenge-groups/{groupId}/my-todos", 1)
                    .param("date", LocalDate.now().toString())
                    .param("status", REVIEW_PENDING.name())
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(createDocument(
                pathParameters(
                    parameterWithName("groupId")
                        .description("챌린지 그룹 id")
                        .attributes(constraints("유효한 챌린지 그룹 id만 입력 가능"), pathVariableExample(1))),
                queryParameters(
                    parameterWithName("date")
                        .description("데일리 투두 날짜"),
                    parameterWithName("status")
                        .description("데일리 투두 상태")
                        .attributes(constraints("옵션으로 정해진 값만 허용"))
                        .attributes(options("CERTIFY_PENDING(인증 대기)", "REVIEW_PENDING(검사 대기)", "APPROVE(인정)", "REJECT(노인정)"))),
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
                    fieldWithPath("data.todos[].canRequestCertificationReview")
                        .description("데일리 투두 인증 검사 요청 가능 여부")
                        .type(JsonFieldType.BOOLEAN),
                    fieldWithPath("data.todos[].certificationContent")
                        .description("데일리 투두 인증글 내용")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos[].certificationMediaUrl")
                        .description("데일리 투두 인증글 이미지 URL")
                        .type(JsonFieldType.STRING))));
    }

    @DisplayName("[V2] 참여중인 특정 챌린지 그룹에 속한 특정 그룹원의 당일 데일리 투두 히스토리 전체 조회 API")
    @Test
    void getChallengeGroupMemberTodayTodoHistoryV2() throws Exception {
        final FindTargetMemberTodayTodoHistoriesDto serviceMockResponse = new FindTargetMemberTodayTodoHistoriesDto(
            3,
            List.of(
                new TodoHistoryDto(1L, 1L, "치킨 먹기", CERTIFY_PENDING.name(), true, false, null, null, true, null),
                new TodoHistoryDto(2L, 2L, "재홍님 갈구기", CERTIFY_PENDING.name(), true, false, null, null, true, null),
                new TodoHistoryDto(3L, 3L, "치킨 먹기", REVIEW_PENDING.name(), false, true, "개꿀맛 치킨 냠냠", "https://치킨.png", true, null),
                new TodoHistoryDto(4L, 4L, "재홍님 갈구기", REVIEW_PENDING.name(), false, false, "아 재홍님 그거 그렇게 하는거 아닌데", "https://갈굼1.png", false, null),
                new TodoHistoryDto(5L, 5L,  "재홍님 갈구기", APPROVE.name(), false, false, "아 재홍님 그거 그렇게 하는거 아닌데", "https://갈굼1.png", false, "재홍님 갈구기 너무 재밌어요"),
                new TodoHistoryDto(6L, 6L,  "치킨 먹기", REJECT.name(), false, false, "개꿀맛 치킨 냠냠", "https://치킨.png", false, "치킨 부럽다ㅠㅠ 심술나서 노인정!")
            )
        );
        given(dailyTodoHistoryService.findAllTodayTodoHistories(any(), any(), any()))
            .willReturn(serviceMockResponse);

        mockMvc.perform(
                get("/api/v2/challenge-groups/{groupId}/challenge-group-members/{challengeGroupMemberId}/today-todo-history", 1, 2)
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(createDocument(
                pathParameters(
                    parameterWithName("groupId")
                        .description("챌린지 그룹 id")
                        .attributes(constraints("유효한 챌린지 그룹 id만 입력 가능"), pathVariableExample(1)),
                    parameterWithName("challengeGroupMemberId")
                        .description("조회할 챌린지 그룹 멤버 id")
                        .attributes(constraints("유효한 챌린지 그룹 멤버 id만 입력 가능"), pathVariableExample(2))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.currentTodoHistoryToReadIndex")
                        .description("현재 읽어야하는 투두 순서 (0부터 시작)")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.todos")
                        .description("조회한 챌린지 그룹 멤버 투두 리스트")
                        .type(JsonFieldType.ARRAY),
                    fieldWithPath("data.todos[].historyId")
                        .description("데일리 투두 히스토리 id")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.todos[].todoId")
                        .description("데일리 투두 id")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.todos[].content")
                        .description("데일리 투두 내용")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos[].status")
                        .description("데일리 투두 상태")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos[].canRequestCertification")
                        .description("데일리 투두 인증 요청 가능 여부")
                        .type(JsonFieldType.BOOLEAN),
                    fieldWithPath("data.todos[].canRequestCertificationReview")
                        .description("데일리 투두 인증 검사 요청 가능 여부")
                        .type(JsonFieldType.BOOLEAN),
                    fieldWithPath("data.todos[].certificationContent")
                        .description("데일리 투두 인증글 내용")
                        .optional()
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos[].certificationMediaUrl")
                        .description("데일리 투두 인증글 이미지 URL")
                        .optional()
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.todos[].isRead")
                        .description("투두 읽음 여부")
                        .type(JsonFieldType.BOOLEAN),
                    fieldWithPath("data.todos[].reviewFeedback")
                        .description("데일리 투두 인증 검사 피드백")
                        .optional()
                        .type(JsonFieldType.STRING))));
    }
}
