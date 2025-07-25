package site.dogether.docs.memberactivity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import site.dogether.docs.util.RestDocsSupport;
import site.dogether.memberactivity.controller.v1.MemberActivityControllerV1;
import site.dogether.memberactivity.controller.v1.dto.response.GetMemberAllStatsResponseV1;
import site.dogether.memberactivity.service.MemberActivityServiceV1;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("사용자 활동 V1 API 문서화 테스트")
class MemberActivityControllerV1DocsTest extends RestDocsSupport {

    private final MemberActivityServiceV1 memberActivityServiceV1 = mock(MemberActivityServiceV1.class);

    @Override
    protected Object initController() {
        return new MemberActivityControllerV1(memberActivityServiceV1);
    }

    @DisplayName("[V1]사용자의 활동 통계 및 작성한 인증 목록 전체 조회 API (투두 완료일 순)")
    @Test
    void getMemberAllStatsSortedByTodoCompletedAt() throws Exception {

        GetMemberAllStatsResponseV1.DailyTodoStats stats = new GetMemberAllStatsResponseV1.DailyTodoStats(
                5,
                3,
                2
        );

        List<GetMemberAllStatsResponseV1.CertificationsGroupedByTodoCompletedAt> certificationsGroupedByTodoCompletedAt = List.of(
                new GetMemberAllStatsResponseV1.CertificationsGroupedByTodoCompletedAt(
                        "2025.05.01",
                        List.of(
                                new GetMemberAllStatsResponseV1.DailyTodoCertificationInfo(
                                        1L,
                                        "운동 하기",
                                        "APPROVE",
                                        "운동 개조짐 ㅋㅋㅋㅋ",
                                        "운동 조지는 짤.png",
                                        "저도 같이 해요..."
                                )
                        )
                ),
                new GetMemberAllStatsResponseV1.CertificationsGroupedByTodoCompletedAt(
                        "2025.05.02",
                        List.of(
                                new GetMemberAllStatsResponseV1.DailyTodoCertificationInfo(
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

        GetMemberAllStatsResponseV1.PageInfoDto pageInfoDto = new GetMemberAllStatsResponseV1.PageInfoDto(
            10,
            0,
            true,
            50
        );


        GetMemberAllStatsResponseV1 response = new GetMemberAllStatsResponseV1(stats, certificationsGroupedByTodoCompletedAt, null, pageInfoDto);

        given(memberActivityServiceV1.getMemberAllStats(any(), any(), any(), any()))
                .willReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/my/activity")
                    .param("sortBy", "TODO_COMPLETED_AT")
                    .param("status", "APPROVE")
                    .param("page", "0")
                    .param("size", "50")
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(createDocument(
                queryParameters(
                    parameterWithName("sortBy")
                        .description("정렬 방식")
                        .attributes(constraints("옵션으로 정해진 값만 허용"))
                        .attributes(options("TODO_COMPLETED_AT(투두 완료일 순)", "GROUP_CREATED_AT(그룹 생성일 순)")),
                    parameterWithName("status")
                        .optional()
                        .description("데일리 투두 상태")
                        .attributes(constraints("옵션으로 정해진 값만 허용"))
                        .attributes(options("REVIEW_PENDING(검사 대기)", "APPROVE(인정)", "REJECT(노인정)")),
                    parameterWithName("page")
                        .description("페이지 번호 (0부터 시작)"),
                    parameterWithName("size")
                        .description("한 페이지에 인증 목록 50개")),
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
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.pageInfo.totalPageCount")
                        .description("전체 페이지 개수")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.pageInfo.recentPageNumber")
                        .description("방금 조회한 페이지 번호")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.pageInfo.hasNext")
                        .description("다음 페이지 여부")
                        .type(JsonFieldType.BOOLEAN),
                    fieldWithPath("data.pageInfo.pageSize")
                        .description("현재 페이지 내 인증 목록 개수")
                        .type(JsonFieldType.NUMBER)
                )));
    }

    @DisplayName("[V1]사용자의 활동 통계 및 작성한 인증 목록 전체 조회 API (그룹 생성일 순)")
    @Test
    void getMemberAllStatsSortedByGroupCreatedAt() throws Exception {

        GetMemberAllStatsResponseV1.DailyTodoStats stats = new GetMemberAllStatsResponseV1.DailyTodoStats(
                5,
                3,
                2
        );

        List<GetMemberAllStatsResponseV1.CertificationsGroupedByGroupCreatedAt> certificationsGroupedByGroupCreatedAt = List.of(
                new GetMemberAllStatsResponseV1.CertificationsGroupedByGroupCreatedAt(
                        "스쿼트 챌린지",
                        List.of(
                                new GetMemberAllStatsResponseV1.DailyTodoCertificationInfo(
                                        1L,
                                        "운동 하기",
                                        "REJECT",
                                        "운동 개조짐 ㅋㅋㅋㅋ",
                                        "운동 조지는 짤.png",
                                        "에이 이건 운동 아니지"
                                )
                        )
                ),
                new GetMemberAllStatsResponseV1.CertificationsGroupedByGroupCreatedAt(
                        "TIL 챌린지",
                        List.of(
                                new GetMemberAllStatsResponseV1.DailyTodoCertificationInfo(
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

        GetMemberAllStatsResponseV1.PageInfoDto pageInfoDto = new GetMemberAllStatsResponseV1.PageInfoDto(
            10,
            0,
            true,
            50
        );

        GetMemberAllStatsResponseV1 response = new GetMemberAllStatsResponseV1(stats, null, certificationsGroupedByGroupCreatedAt, pageInfoDto);

        given(memberActivityServiceV1.getMemberAllStats(any(), any(), any(), any()))
                .willReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/my/activity")
                                .param("sortBy", "GROUP_CREATED_AT")
                                .param("status", "REJECT")
                                .param("page", "0")
                                .param("size", "50")
                                .header("Authorization", "Bearer access_token")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(createDocument(
                        queryParameters(
                                parameterWithName("sortBy")
                                    .description("정렬 방식")
                                    .attributes(constraints("옵션으로 정해진 값만 허용"))
                                    .attributes(options("TODO_COMPLETED_AT(투두 완료일 순)", "GROUP_CREATED_AT(그룹 생성일 순)")),
                                parameterWithName("status")
                                    .optional()
                                    .description("데일리 투두 상태")
                                    .attributes(constraints("옵션으로 정해진 값만 허용"))
                                    .attributes(options("REVIEW_PENDING(검사 대기)", "APPROVE(인정)", "REJECT(노인정)")),
                                parameterWithName("page")
                                    .description("페이지 번호 (0부터 시작)"),
                                parameterWithName("size")
                                    .description("한 페이지에 인증 목록 50개")),
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
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.pageInfo.totalPageCount")
                                    .description("전체 페이지 개수")
                                    .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.pageInfo.recentPageNumber")
                                    .description("방금 조회한 페이지 번호")
                                    .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.pageInfo.hasNext")
                                    .description("다음 페이지 여부")
                                    .type(JsonFieldType.BOOLEAN),
                                fieldWithPath("data.pageInfo.pageSize")
                                    .description("현재 페이지 내 인증 목록 개수")
                                    .type(JsonFieldType.NUMBER)
                        )));
    }
}
