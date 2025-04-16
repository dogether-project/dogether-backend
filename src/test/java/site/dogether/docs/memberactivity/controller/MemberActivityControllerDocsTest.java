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
                                fieldWithPath("groups")
                                        .description("그룹 이름 목록")
                                        .optional()
                                        .type(JsonFieldType.ARRAY),
                                fieldWithPath("data.groups[].id")
                                        .description("챌린지 그룹 id")
                                        .optional()
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.groups[].name")
                                        .description("그룹 이름")
                                        .optional()
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
                        MockMvcRequestBuilders.get("/api/my/{groupId}/activity", groupId)
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
}
