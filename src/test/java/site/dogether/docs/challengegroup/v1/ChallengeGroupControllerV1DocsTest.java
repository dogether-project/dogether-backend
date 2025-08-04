package site.dogether.docs.challengegroup.v1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import site.dogether.challengegroup.controller.v1.ChallengeGroupControllerV1;
import site.dogether.challengegroup.controller.v1.dto.request.CreateChallengeGroupApiRequestV1;
import site.dogether.challengegroup.controller.v1.dto.request.JoinChallengeGroupApiRequestV1;
import site.dogether.challengegroup.controller.v1.dto.request.SaveLastSelectedChallengeGroupInfoApiRequestV1;
import site.dogether.challengegroup.controller.v1.dto.response.CheckParticipatingChallengeGroupApiResponseV1;
import site.dogether.challengegroup.service.ChallengeGroupService;
import site.dogether.challengegroup.service.dto.ChallengeGroupMemberOverviewDto;
import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupsWithLastSelectedGroupIndexDto;
import site.dogether.docs.util.RestDocsSupport;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static site.dogether.dailytodohistory.entity.DailyTodoHistoryReadStatus.*;

@DisplayName("챌린지 그룹 API 문서화 테스트")
public class ChallengeGroupControllerV1DocsTest extends RestDocsSupport {

    private final ChallengeGroupService challengeGroupService = mock(ChallengeGroupService.class);

    @Override
    protected Object initController() {
        return new ChallengeGroupControllerV1(challengeGroupService);
    }

    @DisplayName("챌린지 그룹 생성 API")
    @Test
    void createChallengeGroup() throws Exception {
        final CreateChallengeGroupApiRequestV1 request = new CreateChallengeGroupApiRequestV1(
            "성욱이와 친구들",
            10,
            "TOMORROW",
            3
        );

        given(challengeGroupService.createChallengeGroup(any(CreateChallengeGroupApiRequestV1.class), any()))
                .willReturn("A1Bc4dEf");

        mockMvc.perform(
                post("/api/v1/groups")
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
                        .description("그룹 시작일")
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("옵션으로 정해진 값만 허용"))
                        .attributes(options("TODAY(오늘 시작)", "TOMORROW(내일 시작)")),
                    fieldWithPath("duration")
                        .description("그룹 진행 기간")
                        .type(JsonFieldType.NUMBER)
                        .attributes(constraints("옵션으로 정해진 값만 허용"))
                        .attributes(options("3(3일)", "7(7일)", "14(14일)", "28(28일)"))
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
        final JoinChallengeGroupApiRequestV1 request = new JoinChallengeGroupApiRequestV1("A1Bc4dEf");

        given(challengeGroupService.joinChallengeGroup(any(), any()))
            .willReturn(new JoinChallengeGroupDto(
                    "성욱이와 친구들",
                    3,
                    10,
                    "25.03.02",
                    "25.03.05"
            ));

        mockMvc.perform(
                post("/api/v1/groups/join")
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                requestFields(
                    fieldWithPath("joinCode")
                    .description("참여 코드")
                    .type(JsonFieldType.STRING)
                    .attributes(constraints("서버에서 발급한 숫자,영문 포함 8자리 코드"))),
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
                        .description("총 기간")
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
                    "RUNNING",
                    "25.03.02",
                    "25.03.05",
                    5,
                    0.3),
            new JoiningChallengeGroupDto(
                    2L,
                    "켈리와 친구들",
                    1,
                    10,
                    "A1Bc4dEf",
                    "D_DAY",
                    "25.03.02",
                    "25.03.05",
                    2,
                    0.5)
        );
        final JoiningChallengeGroupsWithLastSelectedGroupIndexDto joiningChallengeGroupsWithLastSelectedGroupIndexDto = new JoiningChallengeGroupsWithLastSelectedGroupIndexDto(1, joiningChallengeGroups);

        given(challengeGroupService.getJoiningChallengeGroups(any()))
            .willReturn(joiningChallengeGroupsWithLastSelectedGroupIndexDto);

        mockMvc.perform(
                get("/api/v1/groups/my")
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
                    fieldWithPath("data.lastSelectedGroupIndex")
                        .description("가장 마지막에 선택한 챌린지 그룹 인덱스")
                        .type(JsonFieldType.NUMBER)
                        .optional(),
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
                    fieldWithPath("data.joiningChallengeGroups[].status")
                        .description("그룹 상태")
                        .type(JsonFieldType.STRING)
                        .attributes(options("READY(시작 전)", "RUNNING(진행중)", "D_DAY(오늘 종료)", "FINISHED(종료)")),
                    fieldWithPath("data.joiningChallengeGroups[].startAt")
                        .description("챌린지 시작일")
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

    @DisplayName("사용자가 가장 마지막에 선택한 챌린지 그룹 id 저장 API")
    @Test
    void saveLastSelectedChallengeGroupInfo() throws Exception {
        final SaveLastSelectedChallengeGroupInfoApiRequestV1 request = new SaveLastSelectedChallengeGroupInfoApiRequestV1(1L);

        mockMvc.perform(
                post("/api/v1/groups/last-selected")
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                requestFields(
                    fieldWithPath("groupId")
                        .description("사용자가 마지막에 선택한 챌린지 그룹 id")
                        .type(JsonFieldType.NUMBER)
                        .attributes(constraints("현재 진행중이면서 사용자가 참여중인 챌린지 그룹 id만 입력 허용"))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING))));
    }

    @DisplayName("챌린지 그룹 탈퇴 API")
    @Test
    void leaveChallengeGroup() throws Exception {
        final Long groupId = 1L;

        mockMvc.perform(
                delete("/api/v1/groups/{groupId}/leave", groupId)
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(createDocument(
                    pathParameters(
                        parameterWithName("groupId")
                            .description("탈퇴할 챌린지 그룹의 ID")
                                .attributes(constraints("존재하는 챌린지 그룹 id만 입력 가능"), pathVariableExample(groupId))),
                    responseFields(
                        fieldWithPath("code")
                            .description("응답 코드")
                            .type(JsonFieldType.STRING),
                        fieldWithPath("message")
                            .description("응답 메시지")
                            .type(JsonFieldType.STRING))));
    }

    @DisplayName("챌린지 그룹 참여 여부 조회 API")
    @Test
    void checkParticipatingChallengeGroup() throws Exception {
        given(challengeGroupService.checkParticipatingChallengeGroup(any()))
            .willReturn(new CheckParticipatingChallengeGroupApiResponseV1(true));

        mockMvc.perform(
                get("/api/v1/groups/participating")
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
                    fieldWithPath("data.checkParticipating")
                        .description("참여 필요 여부")
                        .type(JsonFieldType.BOOLEAN))));
    }

    @DisplayName("참여중인 특정 챌린지 그룹의 그룹원 전체 랭킹 조회 API")
    @Test
    void getJoiningChallengeGroupTeamActivitySummary() throws Exception {
        final List<ChallengeGroupMemberOverviewDto> groupMemberRanks = List.of(
            new ChallengeGroupMemberOverviewDto(
                1L,
                1,
                "성욱이의 셀카.png",
                "성욱",
                NULL,
                100
            ),
            new ChallengeGroupMemberOverviewDto(
                2L,
                2,
                "고양이.png",
                "영재",
                READ_ALL,
                80
            ),
            new ChallengeGroupMemberOverviewDto(
                3L,
                3,
                "그로밋.png",
                "서은",
                READ_YET,
                60
            )
        );

        given(challengeGroupService.getChallengeGroupMemberOverview(any(), any()))
                .willReturn(groupMemberRanks);

        mockMvc.perform(
                get("/api/v1/groups/{groupId}/ranking", 1)
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(createDocument(
                pathParameters(
                    parameterWithName("groupId")
                        .description("챌린지 그룹 id")
                            .attributes(constraints("존재하는 챌린지 그룹 id만 입력 가능"), pathVariableExample(1))),
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
                    fieldWithPath("data.ranking[].memberId")
                        .description("그룹원 ID")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.ranking[].rank")
                        .description("순위")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.ranking[].profileImageUrl")
                            .description("사용자 프로필 사진")
                            .type(JsonFieldType.STRING),
                    fieldWithPath("data.ranking[].name")
                        .description("그룹원 이름")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.ranking[].historyReadStatus")
                        .description("히스토리 읽음 상태 {옵션: NULL, READYET, READALL}")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.ranking[].achievementRate")
                        .description("데일리 투두 인증률")
                        .type(JsonFieldType.NUMBER))
            ));
    }
}
