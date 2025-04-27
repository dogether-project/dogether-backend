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
import site.dogether.challengegroup.controller.response.ChallengeGroupMemberRankResponse;
import site.dogether.challengegroup.controller.response.GetChallengeGroupMembersRank;
import site.dogether.challengegroup.service.ChallengeGroupService;
import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupMyActivityDto;
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
            8,
            "TOMORROW",
            3
        );

        given(challengeGroupService.createChallengeGroup(any(CreateChallengeGroupRequest.class), any()))
                .willReturn("A1Bc4dEf");

        mockMvc.perform(
                post("/api/groups")
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                requestFields(
                    fieldWithPath("groupName")
                        .description("그룹명")
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("1 ~ 10 길이 문자열")),
                    fieldWithPath("maximumMemberCount")
                        .description("참여 가능 인원수")
                        .type(JsonFieldType.NUMBER)
                        .attributes(constraints("2 ~ 20 범위 정수")),
                    fieldWithPath("startAt")
                        .description(generateLink(CHALLENGE_GROUP_START_AT_OPTION))
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("정해진 값만 입력 허용")),
                    fieldWithPath("duration")
                        .description(generateLink(CHALLENGE_GROUP_DURATION_OPTION))
                        .type(JsonFieldType.NUMBER)
                        .attributes(constraints("정해진 값만 입력 허용"))
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
        final JoinChallengeGroupRequest request = new JoinChallengeGroupRequest("A1Bc4dEf");

        given(challengeGroupService.joinChallengeGroup(any(), any()))
            .willReturn(new JoinChallengeGroupDto(
                    "성욱이와 친구들",
                    3,
                    8,
                    "25.03.02",
                    "25.03.05"
            ));

        mockMvc.perform(
                post("/api/groups/members/me")
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                requestFields(
                    fieldWithPath("joinCode")
                        .description("그룹 참여 코드")
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("서버에서 발급한 코드(영문, 숫자 조합 8자리)"))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.groupName")
                        .description("그룹명")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.duration")
                        .description("챌린지 기간")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.maximumMemberCount")
                        .description("총 인원수")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.startAt")
                        .description("챌린지 시작일")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.endAt")
                        .description("챌린지 종료일")
                        .type(JsonFieldType.STRING))));
    }

    @DisplayName("참여중인 챌린지 그룹 정보 전체 조회 API")
    @Test
    void getJoiningChallengeGroups() throws Exception {
        List<JoiningChallengeGroupDto> joiningChallengeGroups = List.of(
            new JoiningChallengeGroupDto(
                    1L,
                    "폰트의 챌린지",
                    1,
                    10,
                    "G3hIj4kLm",
                    "25.03.05",
                    5,
                    0.3),
            new JoiningChallengeGroupDto(
                    2L,
                    "켈리와 친구들",
                    1,
                    10,
                    "A1Bc4dEf",
                    "25.03.02",
                    2,
                    0.5)
        );

        given(challengeGroupService.getJoiningChallengeGroups(any()))
            .willReturn(joiningChallengeGroups);

        mockMvc.perform(
                get("/api/groups/members/me")
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
                    fieldWithPath("data.joiningChallengeGroups")
                        .description("참여중인 챌린지 그룹")
                        .type(JsonFieldType.ARRAY)
                        .optional(),
                    fieldWithPath("data.joiningChallengeGroups[].groupId")
                        .description("챌린지 그룹 id")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.joiningChallengeGroups[].groupName")
                        .description("그룹명")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.joiningChallengeGroups[].currentMemberCount")
                        .description("현재 인원수")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.joiningChallengeGroups[].maximumMemberCount")
                        .description("총 인원수")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.joiningChallengeGroups[].joinCode")
                        .description("그룹 참여 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.joiningChallengeGroups[].endAt")
                        .description("챌린지 종료일")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.joiningChallengeGroups[].progressDay")
                        .description("활동 진행 일수")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.joiningChallengeGroups[].progressRate")
                        .description("활동 진행률")
                        .type(JsonFieldType.NUMBER)
                )));
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

        List<ChallengeGroupMemberRankResponse> groupMemberRanks = List.of(
                new ChallengeGroupMemberRankResponse(1, "성욱이의 셀카.png", "성욱", 100),
                new ChallengeGroupMemberRankResponse(2, "고양이.png", "영재", 80),
                new ChallengeGroupMemberRankResponse(3, "그로밋.png", "서은", 60)
        );
        GetChallengeGroupMembersRank response = new GetChallengeGroupMembersRank(groupMemberRanks);

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
                        .type(JsonFieldType.ARRAY),
                    fieldWithPath("data.ranking[].rank")
                        .description("순위")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.ranking[].profileImageUrl")
                            .description("사용자 프로필 사진")
                            .type(JsonFieldType.STRING),
                    fieldWithPath("data.ranking[].name")
                        .description("그룹원 이름")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.ranking[].achievementRate")
                        .description("데일리 투두 인증률")
                        .type(JsonFieldType.NUMBER))
            ));
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
