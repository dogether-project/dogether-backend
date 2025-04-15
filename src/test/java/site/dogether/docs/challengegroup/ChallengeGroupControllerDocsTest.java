package site.dogether.docs.challengegroup;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static site.dogether.docs.util.DocumentLinkGenerator.DocUrl.CHALLENGE_GROUP_DURATION_OPTION;
import static site.dogether.docs.util.DocumentLinkGenerator.DocUrl.CHALLENGE_GROUP_START_AT_OPTION;
import static site.dogether.docs.util.DocumentLinkGenerator.generateLink;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import site.dogether.challengegroup.controller.ChallengeGroupController;
import site.dogether.challengegroup.controller.request.CreateChallengeGroupRequest;
import site.dogether.challengegroup.controller.request.JoinChallengeGroupRequest;
import site.dogether.challengegroup.service.ChallengeGroupService;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupTeamActivityDto;
import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupInfo;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupMyActivityDto;
import site.dogether.dailytodo.entity.Rank;
import site.dogether.docs.util.RestDocsSupport;

@DisplayName("챌린지 그룹 API 문서화 테스트")
public class ChallengeGroupControllerDocsTest extends RestDocsSupport {

    private final ChallengeGroupService challengeGroupService = mock(ChallengeGroupService.class);

    @Override
    protected Object initController() {
        return new ChallengeGroupController(challengeGroupService);
    }

    @DisplayName("챌린지 그룹 생성 API")
    @Test
    void createChallengeGroup() throws Exception {
        final CreateChallengeGroupRequest request = new CreateChallengeGroupRequest(
            "성욱이와 친구들",
            7,
            "TODAY",
            7,
            5
        );

        given(challengeGroupService.createChallengeGroup(any(CreateChallengeGroupRequest.class), any()))
                .willReturn("Join Code");

        mockMvc.perform(
                post("/api/groups")
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                requestFields(
                    fieldWithPath("name")
                        .description("그룹명")
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("1 ~ 20 길이 문자열")),
                    fieldWithPath("maximumMemberCount")
                        .description("참여 가능 인원수")
                        .type(JsonFieldType.NUMBER)
                        .attributes(constraints("2 ~ 20 범위 정수")),
                    fieldWithPath("startAt")
                        .description(generateLink(CHALLENGE_GROUP_START_AT_OPTION))
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("정해진 값만 입력 허용")),
                    fieldWithPath("durationOption")
                        .description(generateLink(CHALLENGE_GROUP_DURATION_OPTION))
                        .type(JsonFieldType.NUMBER)
                        .attributes(constraints("정해진 값만 입력 허용")),
                    fieldWithPath("maximumTodoCount")
                        .description("하루 최대 작성 가능 투두 개수")
                        .type(JsonFieldType.NUMBER)
                        .attributes(constraints("2 ~ 10 범위 정수"))
                ),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.joinCode")
                        .description("그룹 참여 코드")
                        .type(JsonFieldType.STRING))));
    }

    @DisplayName("챌린지 그룹 참가 API")
    @Test
    void joinChallengeGroup() throws Exception {
        final JoinChallengeGroupRequest request = new JoinChallengeGroupRequest("kelly-join-code");

        given(challengeGroupService.joinChallengeGroup(any(), any()))
            .willReturn(new JoinChallengeGroupDto("성욱이와 친구들", 7, "2025.02.17", "2025.02.23", 7));

        mockMvc.perform(
                post("/api/groups/join")
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                requestFields(
                    fieldWithPath("joinCode")
                        .description("그룹 참여 코드")
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("서버에서 발급한 코드만 사용 갸능"))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.name")
                        .description("그룹명")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.maximumMemberCount")
                        .description("총 인원수")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.startAt")
                        .description("챌린지 시작일")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.endAt")
                        .description("챌린지 종료일")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.durationOption")
                        .description("챌린지 기간")
                        .type(JsonFieldType.NUMBER))));
    }

    @DisplayName("참여중인 그룹 정보 조회 API")
    @Test
    void getJoiningChallengeGroupInfo() throws Exception {
        given(challengeGroupService.getJoiningChallengeGroupInfo(any()))
            .willReturn(new JoiningChallengeGroupInfo("성욱이와 친구들", 7, "Join Code", 5, "25.02.25", 5));

        mockMvc.perform(
                get("/api/groups/info/current")
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
                    fieldWithPath("data.name")
                        .description("그룹명")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.duration")
                        .description("챌린지 기간")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.joinCode")
                        .description("그룹 참여 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.maximumTodoCount")
                        .description("작성 가능한 하루 투두 제한 개수")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.endAt")
                        .description("챌린지 종료일")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.remainingDays")
                        .description("남은 일수")
                        .type(JsonFieldType.NUMBER))));
    }

    @DisplayName("참여중인 그룹의 내 누적 활동 통계 조회 API")
    @Test
    void getJoiningChallengeGroupMyActivitySummary() throws Exception {
        given(challengeGroupService.getJoiningChallengeGroupMyActivitySummary(any()))
            .willReturn(new JoiningChallengeGroupMyActivityDto(10, 5, 3, 2));

        mockMvc.perform(
                get("/api/groups/summary/my")
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
                    fieldWithPath("data.totalTodoCount")
                        .description("작성한 전체 투두 개수")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.totalCertificatedCount")
                        .description("인증한 전체 투두 개수")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.totalApprovedCount")
                        .description("인정받은 투두 개수")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.totalRejectedCount")
                        .description("노인정 투두 개수")
                        .type(JsonFieldType.NUMBER))));
    }

    @DisplayName("참여중인 특정 챌린지 그룹의 그룹원 전체 랭킹 조회 API")
    @Test
    void getJoiningChallengeGroupTeamActivitySummary() throws Exception {
        final long groupId = 1L;

        given(challengeGroupService.getJoiningChallengeGroupTeamActivitySummary(any()))
            .willReturn(new JoiningChallengeGroupTeamActivityDto(
                List.of(
                    new Rank(1, "성욱", 80),
                    new Rank(2, "영재", 50),
                    new Rank(3, "지원", 30)
            )));

        mockMvc.perform(
                get("/api/groups/{groupId}/ranking", groupId)
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(createDocument(
                pathParameters(
                    parameterWithName("groupId")
                        .description("챌린지 그룹 id")
                            .attributes(constraints("존재하는 챌린지 그룹 id만 입력 가능"), pathVariableExample(groupId))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.ranking")
                        .description("그룹 내 활동 순위")
                        .type(JsonFieldType.ARRAY)
                        .optional(),
                    fieldWithPath("data.ranking[].rank")
                        .description("순위")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.ranking[].name")
                        .description("그룹원 이름")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.ranking[].certificationRate")
                        .description("데일리 투두 인증률")
                        .type(JsonFieldType.NUMBER))
            ));
    }


    @DisplayName("챌린지 그룹 참여 여부 조회 API")
    @Test
    void isJoiningChallengeGroup() throws Exception {
        given(challengeGroupService.isJoiningChallengeGroup(any()))
                .willReturn(true);

        mockMvc.perform(
                        get("/api/groups/isJoining")
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
                            fieldWithPath("data.isJoining")
                                .description("그룹에 참여중인지 여부")
                                .type(JsonFieldType.BOOLEAN))));
    }

    @DisplayName("챌린지 그룹 탈퇴 API")
    @Test
    void leaveChallengeGroup() throws Exception {
        mockMvc.perform(
                delete("/api/groups/leave")
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
                        .type(JsonFieldType.STRING))));
    }
}
