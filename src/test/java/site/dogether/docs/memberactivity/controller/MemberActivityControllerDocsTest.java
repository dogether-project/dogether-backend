package site.dogether.docs.memberactivity.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import site.dogether.docs.util.RestDocsSupport;
import site.dogether.memberactivity.controller.MemberActivityController;
import site.dogether.memberactivity.controller.response.*;

import java.util.List;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberActivityControllerDocsTest extends RestDocsSupport {

    @Override
    protected Object initController() {
        return new MemberActivityController();
    }

    @DisplayName("참여중인 챌린지 그룹 목록 조회 API")
    @Test
    void getAllGroupsName() throws Exception {
        final List<GroupNameResponse> groups = List.of(
                new GroupNameResponse(1L, "성욱이와 친구들"),
                new GroupNameResponse(2L, "스콘 먹기 챌린지"),
                new GroupNameResponse(3L, "성욱이의 일기")
        );
        GetAllGroupNamesResponse response = new GetAllGroupNamesResponse(groups);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/my/groups")
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
                                fieldWithPath("data.groups")
                                        .description("그룹 이름 목록")
                                        .optional()
                                        .type(JsonFieldType.ARRAY),
                                fieldWithPath("data.groups[].id")
                                        .description("챌린지 그룹 id")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.groups[].name")
                                        .description("그룹 이름")
                                        .type(JsonFieldType.STRING))));
    }

    @DisplayName("참여중인 특정 챌린지 그룹 활동 통계 조회 API")
    @Test
    void getGroupActivityStat() throws Exception {
        final long groupId = 1L;

        final List<CertificationPeriodResponse> certificationPeriods = List.of(
                new CertificationPeriodResponse(1, 8, 2, 25),
                new CertificationPeriodResponse(2, 6, 3, 50),
                new CertificationPeriodResponse(3, 3, 3, 100)
        );

        final RankingResponse ranking = new RankingResponse(10, 3);
        final MemberStatsResponse stats = new MemberStatsResponse(123, 123, 123);

        final GetGroupActivityStatResponse response = new GetGroupActivityStatResponse(
                "성욱이와 친구들",
                "25.02.25",
                certificationPeriods,
                ranking,
                stats
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/my//groups/{groupId}/activity", groupId)
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
                                fieldWithPath("data.name")
                                        .description("그룹 이름")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.endAt")
                                        .description("종료일")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.certificationPeriods")
                                        .description("인증한 기간 통계")
                                        .optional()
                                        .type(JsonFieldType.ARRAY),
                                fieldWithPath("data.certificationPeriods[].day")
                                        .description("일차")
                                        .optional()
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.certificationPeriods[].createdCount")
                                        .description("작성한 투두 개수")
                                        .optional()
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.certificationPeriods[].certificatedCount")
                                        .description("인증한 투두 개수")
                                        .optional()
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.certificationPeriods[].certificationRate")
                                        .description("달성률")
                                        .optional()
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.ranking.totalMemberCount")
                                        .description("그룹원 수")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.ranking.myRank")
                                        .description("내 랭킹")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.stats.certificatedCount")
                                        .description("인증한 투두 개수")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.stats.approvedCount")
                                        .description("인정받은 투두 개수")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.stats.rejectedCount")
                                        .description("노인정 받은 투두 개수")
                                        .type(JsonFieldType.NUMBER))));
    }

    @DisplayName("사용자의 활동 통계 및 작성한 인증 목록 전체 조회 API")
    @Test
    void getMemberAllStats() throws Exception {
        final GetMemberAllStatsResponse.DailyTodoStats stats = new GetMemberAllStatsResponse.DailyTodoStats(
                5,
                3,
                2
        );

        final List<GetMemberAllStatsResponse.DailyTodoCertifications> certifications = List.of(
                new GetMemberAllStatsResponse.DailyTodoCertifications(
                        1L,
                        "운동 하기",
                        "REVIEW_PENDING",
                        "운동 개조짐 ㅋㅋㅋㅋ",
                        "운동 조지는 짤.png",
                        null
                ),
                new GetMemberAllStatsResponse.DailyTodoCertifications(
                        2L,
                        "인강 듣기",
                        "APPROVE",
                        "인강 진짜 열심히 들었습니다. ㅎ",
                        "인강 달리는 짤.png",
                        null
                ),
                new GetMemberAllStatsResponse.DailyTodoCertifications(
                        3L,
                        "DND API 구현",
                        "REJECT",
                        "API 좀 잘 만든듯 ㅋ",
                        "API 명세짤.png",
                        "아 별론데?"
                )
        );

        final GetMemberAllStatsResponse response = new GetMemberAllStatsResponse(stats, certifications);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/my/activity")
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
                                fieldWithPath("data.dailyTodoStats.totalCertificatedCount")
                                        .description("사용자의 인증 목록 개수")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.dailyTodoStats.totalApprovedCount")
                                        .description("사용자의 인정받은 투두 개수")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.dailyTodoStats.totalRejectedCount")
                                        .description("사용자의 노인정 받은 투두 개수")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.dailyTodoCertifications")
                                        .description("개인 인증 목록 전체 조회")
                                        .optional()
                                        .type(JsonFieldType.ARRAY),
                                fieldWithPath("data.dailyTodoCertifications[].id")
                                        .description("데일리 투두 id")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.dailyTodoCertifications[].content")
                                        .description("데일리 투두 내용")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.dailyTodoCertifications[].status")
                                        .description("데일리 투두 상태")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.dailyTodoCertifications[].certificationContent")
                                        .description("데일리 투두 인증글 내용")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.dailyTodoCertifications[].certificationMediaUrl")
                                        .description("데일리 투두 인증글 이미지 URL")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.dailyTodoCertifications[]rejectReason")
                                        .description("데일리 투두 인증 노인정 사유")
                                        .optional()
                                        .type(JsonFieldType.STRING)
                        )));
    }
}
