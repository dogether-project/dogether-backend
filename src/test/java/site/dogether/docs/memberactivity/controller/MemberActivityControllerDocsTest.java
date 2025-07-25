package site.dogether.docs.memberactivity.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import site.dogether.docs.util.RestDocsSupport;
import site.dogether.memberactivity.controller.v0.MemberActivityController;
import site.dogether.memberactivity.controller.v0.dto.response.GetGroupActivityStatResponse;
import site.dogether.memberactivity.controller.v0.dto.response.GetMemberAllStatsResponse;
import site.dogether.memberactivity.service.MemberActivityService;
import site.dogether.memberactivity.service.dto.FindMyProfileDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("사용자 활동 API 문서화 테스트")
class MemberActivityControllerDocsTest extends RestDocsSupport {

    private final MemberActivityService memberActivityService = mock(MemberActivityService.class);

    @Override
    protected Object initController() {
        return new MemberActivityController(memberActivityService);
    }

    @DisplayName("참여중인 특정 챌린지 그룹 활동 통계 조회 API")
    @Test
    void getGroupActivityStat() throws Exception {
        GetGroupActivityStatResponse.ChallengeGroupInfoResponse groupInfo = new GetGroupActivityStatResponse.ChallengeGroupInfoResponse(
                "그로밋과 함께하는 챌린지",
                10,
                6,
                "123456",
                "25.02.22"
        );

        List<GetGroupActivityStatResponse.CertificationPeriodResponse> certificationPeriods = List.of(
                new GetGroupActivityStatResponse.CertificationPeriodResponse(1, 8, 2, 25),
                new GetGroupActivityStatResponse.CertificationPeriodResponse(2, 6, 3, 50),
                new GetGroupActivityStatResponse.CertificationPeriodResponse(3, 6, 3, 50),
                new GetGroupActivityStatResponse.CertificationPeriodResponse(4, 3, 3, 100)
        );

        GetGroupActivityStatResponse.RankingResponse ranking = new GetGroupActivityStatResponse.RankingResponse(10, 3);
        GetGroupActivityStatResponse.MemberStatsResponse stats = new GetGroupActivityStatResponse.MemberStatsResponse(123, 123, 123);

        GetGroupActivityStatResponse response = new GetGroupActivityStatResponse(
                groupInfo,
                certificationPeriods,
                ranking,
                stats
        );

        given(memberActivityService.getGroupActivityStat(any(), any()))
                .willReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/my//groups/{groupId}/activity", 1)
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
                                fieldWithPath("data.groupInfo.name")
                                        .description("그룹 이름")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.groupInfo.maximumMemberCount")
                                        .description("그룹 참여 가능 인원수")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.groupInfo.currentMemberCount")
                                        .description("현재 그룹 인원수")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.groupInfo.joinCode")
                                        .description("초대 코드")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.groupInfo.endAt")
                                        .description("종료일")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.certificationPeriods")
                                        .description("인증한 기간 통계")
                                        .optional()
                                        .type(JsonFieldType.ARRAY),
                                fieldWithPath("data.certificationPeriods[].day")
                                        .description("일차")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.certificationPeriods[].createdCount")
                                        .description("작성한 투두 개수")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.certificationPeriods[].certificatedCount")
                                        .description("인증한 투두 개수")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.certificationPeriods[].certificationRate")
                                        .description("달성률")
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

    @DisplayName("사용자의 활동 통계 및 작성한 인증 목록 전체 조회 API (투두 완료일 순)")
    @Test
    void getMemberAllStatsSortedByTodoCompletedAt() throws Exception {

        GetMemberAllStatsResponse.DailyTodoStats stats = new GetMemberAllStatsResponse.DailyTodoStats(
                5,
                3,
                2
        );

        List<GetMemberAllStatsResponse.CertificationsGroupedByTodoCompletedAt> certificationsGroupedByTodoCompletedAt = List.of(
                new GetMemberAllStatsResponse.CertificationsGroupedByTodoCompletedAt(
                        "2025.05.01",
                        List.of(
                                new GetMemberAllStatsResponse.DailyTodoCertificationInfo(
                                        1L,
                                        "운동 하기",
                                        "APPROVE",
                                        "운동 개조짐 ㅋㅋㅋㅋ",
                                        "운동 조지는 짤.png",
                                        "저도 같이 해요..."
                                )
                        )
                ),
                new GetMemberAllStatsResponse.CertificationsGroupedByTodoCompletedAt(
                        "2025.05.02",
                        List.of(
                                new GetMemberAllStatsResponse.DailyTodoCertificationInfo(
                                        2L,
                                        "인강 듣기",
                                        "APPROVE",
                                        "인강 진짜 열심히 들었습니다. ㅎ",
                                        "인강 달리는 짤.png",
                                        "얼마나 더 똑똑해지려고 ㄷㄷ"
                                )
                        )
                )
        );


        GetMemberAllStatsResponse response = new GetMemberAllStatsResponse(stats, certificationsGroupedByTodoCompletedAt, null);

        given(memberActivityService.getMemberAllStats(any(), any(), any()))
                .willReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/my/activity")
                                .param("sort", "TODO_COMPLETED_AT")
                                .param("status", "APPROVE")
                                .header("Authorization", "Bearer access_token")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(createDocument(
                        queryParameters(
                                parameterWithName("sort")
                                    .description("정렬 방식")
                                    .attributes(constraints("옵션으로 정해진 값만 허용"))
                                    .attributes(options("TODO_COMPLETED_AT(투두 완료일 순)", "GROUP_CREATED_AT(그룹 생성일 순)")),
                                parameterWithName("status")
                                    .optional()
                                    .description("데일리 투두 상태")
                                    .attributes(constraints("옵션으로 정해진 값만 허용"))
                                    .attributes(options("REVIEW_PENDING(검사 대기)", "APPROVE(인정)", "REJECT(노인정)"))),
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
                                fieldWithPath("data.certificationsGroupedByTodoCompletedAt")
                                        .description("인증한 투두 목록")
                                        .optional()
                                        .type(JsonFieldType.ARRAY),
                                fieldWithPath("data.certificationsGroupedByTodoCompletedAt[].createdAt")
                                        .description("투두 완료일")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.certificationsGroupedByTodoCompletedAt[].certificationInfo")
                                        .description("투두 인증 정보")
                                        .type(JsonFieldType.ARRAY),
                                fieldWithPath("data.certificationsGroupedByTodoCompletedAt[].certificationInfo[].id")
                                        .description("데일리 투두 id")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.certificationsGroupedByTodoCompletedAt[].certificationInfo[].content")
                                        .description("데일리 투두 내용")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.certificationsGroupedByTodoCompletedAt[].certificationInfo[].status")
                                        .description("데일리 투두 상태")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.certificationsGroupedByTodoCompletedAt[].certificationInfo[].certificationContent")
                                        .description("데일리 투두 인증글 내용")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.certificationsGroupedByTodoCompletedAt[].certificationInfo[].certificationMediaUrl")
                                        .description("데일리 투두 인증글 이미지 URL")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.certificationsGroupedByTodoCompletedAt[].certificationInfo[].reviewFeedback")
                                        .description("데일리 투두 인증 검사 피드백")
                                        .optional()
                                        .type(JsonFieldType.STRING))));
    }

    @DisplayName("사용자의 활동 통계 및 작성한 인증 목록 전체 조회 API (그룹 생성일 순)")
    @Test
    void getMemberAllStatsSortedByGroupCreatedAt() throws Exception {

        GetMemberAllStatsResponse.DailyTodoStats stats = new GetMemberAllStatsResponse.DailyTodoStats(
                5,
                3,
                2
        );

        List<GetMemberAllStatsResponse.CertificationsGroupedByGroupCreatedAt> certificationsGroupedByGroupCreatedAt = List.of(
                new GetMemberAllStatsResponse.CertificationsGroupedByGroupCreatedAt(
                        "스쿼트 챌린지",
                        List.of(
                                new GetMemberAllStatsResponse.DailyTodoCertificationInfo(
                                        1L,
                                        "운동 하기",
                                        "REJECT",
                                        "운동 개조짐 ㅋㅋㅋㅋ",
                                        "운동 조지는 짤.png",
                                        "에이 이건 운동 아니지"
                                )
                        )
                ),
                new GetMemberAllStatsResponse.CertificationsGroupedByGroupCreatedAt(
                        "TIL 챌린지",
                        List.of(
                                new GetMemberAllStatsResponse.DailyTodoCertificationInfo(
                                        2L,
                                        "인강 듣기",
                                        "REJECT",
                                        "인강 진짜 열심히 들었습니다. ㅎ",
                                        "인강 달리는 짤.png",
                                        "우리 오늘 인강 듣는날 아닌데?"
                                )
                        )
                )
        );

        GetMemberAllStatsResponse response = new GetMemberAllStatsResponse(stats, null, certificationsGroupedByGroupCreatedAt);

        given(memberActivityService.getMemberAllStats(any(), any(), any()))
                .willReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/my/activity")
                                .param("sort", "GROUP_CREATED_AT")
                                .param("status", "REJECT")
                                .header("Authorization", "Bearer access_token")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(createDocument(
                        queryParameters(
                                parameterWithName("sort")
                                    .description("정렬 방식")
                                    .attributes(constraints("옵션으로 정해진 값만 허용"))
                                    .attributes(options("TODO_COMPLETED_AT(투두 완료일 순)", "GROUP_CREATED_AT(그룹 생성일 순)")),
                                parameterWithName("status")
                                    .optional()
                                    .description("데일리 투두 상태")
                                    .attributes(constraints("옵션으로 정해진 값만 허용"))
                                    .attributes(options("REVIEW_PENDING(검사 대기)", "APPROVE(인정)", "REJECT(노인정)"))),
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
                                fieldWithPath("data.certificationsGroupedByGroupCreatedAt")
                                        .description("인증한 투두 목록")
                                        .optional()
                                        .type(JsonFieldType.ARRAY),
                                fieldWithPath("data.certificationsGroupedByGroupCreatedAt[].groupName")
                                        .description("챌린지 그룹명")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.certificationsGroupedByGroupCreatedAt[].certificationInfo")
                                        .description("투두 인증 정보")
                                        .type(JsonFieldType.ARRAY),
                                fieldWithPath("data.certificationsGroupedByGroupCreatedAt[].certificationInfo[].id")
                                        .description("데일리 투두 id")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.certificationsGroupedByGroupCreatedAt[].certificationInfo[].content")
                                        .description("데일리 투두 내용")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.certificationsGroupedByGroupCreatedAt[].certificationInfo[].status")
                                        .description("데일리 투두 상태")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.certificationsGroupedByGroupCreatedAt[].certificationInfo[].certificationContent")
                                        .description("데일리 투두 인증글 내용")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.certificationsGroupedByGroupCreatedAt[].certificationInfo[].certificationMediaUrl")
                                        .description("데일리 투두 인증글 이미지 URL")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.certificationsGroupedByGroupCreatedAt[].certificationInfo[].reviewFeedback")
                                        .description("데일리 투두 인증 검사 피드백")
                                        .optional()
                                        .type(JsonFieldType.STRING))));
    }

    @DisplayName("사용자 프로필 조회 API")
    @Test
    void getMyProfile() throws Exception {

        FindMyProfileDto myProfileDto = new FindMyProfileDto(
                "그로밋",
                "그로밋의 셀카.png"
        );

        given(memberActivityService.getMyProfile(any()))
                .willReturn(myProfileDto);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/my/profile")
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
                                        .description("이름")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.profileImageUrl")
                                        .description("프로필 이미지")
                                        .type(JsonFieldType.STRING))));
    }
}
