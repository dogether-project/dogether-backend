package site.dogether.docs.memberactivity.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import site.dogether.docs.util.RestDocsSupport;
import site.dogether.memberactivity.controller.MemberActivityController;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberActivityControllerDocsTest extends RestDocsSupport {

    @Override
    protected Object initController() {
        return new MemberActivityController();
    }

    @DisplayName("참여중인 특정 챌린지 그룹 활동 통계 조회 API")
    @Test
    void getGroupActivityStat() throws Exception {
        final long groupId = 1L;

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

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/my/activity")
                                .param("sort", "todo-completed-at")
                                .param("status", "approve")
                                .header("Authorization", "Bearer access_token")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(createDocument(
                        queryParameters(
                                parameterWithName("sort")
                                        .description("정렬 방식 {옵션 : todo-completed-at, group-created-at}"),
                                parameterWithName("status")
                                        .optional()
                                        .description("데일리 투두 상태 {옵션: approve, reject, review_pending}")),
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
                                        .description("인증한 투두 목록")
                                        .optional()
                                        .type(JsonFieldType.ARRAY),
                                fieldWithPath("data.dailyTodoCertifications[].createdAt")
                                        .description("투두 완료일")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.dailyTodoCertifications[].certificationInfo")
                                        .description("투두 인증 정보")
                                        .type(JsonFieldType.ARRAY),
                                fieldWithPath("data.dailyTodoCertifications[].certificationInfo[].id")
                                        .description("데일리 투두 id")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.dailyTodoCertifications[].certificationInfo[].content")
                                        .description("데일리 투두 내용")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.dailyTodoCertifications[].certificationInfo[].status")
                                        .description("데일리 투두 상태")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.dailyTodoCertifications[].certificationInfo[].certificationContent")
                                        .description("데일리 투두 인증글 내용")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.dailyTodoCertifications[].certificationInfo[].certificationMediaUrl")
                                        .description("데일리 투두 인증글 이미지 URL")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.dailyTodoCertifications[].certificationInfo[].rejectReason")
                                        .description("데일리 투두 인증 노인정 사유")
                                        .optional()
                                        .type(JsonFieldType.STRING))));
    }

    @DisplayName("사용자의 활동 통계 및 작성한 인증 목록 전체 조회 API (그룹 생성일 순)")
    @Test
    void getMemberAllStatsSortedByGroupCreatedAt() throws Exception {

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/my/activity")
                                .param("sort", "group-created-at")
                                .param("status", "reject")
                                .header("Authorization", "Bearer access_token")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(createDocument(
                        queryParameters(
                                parameterWithName("sort")
                                        .description("정렬 방식 {옵션 : todo-completed-at, group-created-at}"),
                                parameterWithName("status")
                                        .optional()
                                        .description("데일리 투두 상태 {옵션: approve, reject, review_pending}")),
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
                                        .description("인증한 투두 목록")
                                        .optional()
                                        .type(JsonFieldType.ARRAY),
                                fieldWithPath("data.dailyTodoCertifications[].groupName")
                                        .description("챌린지 그룹명")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.dailyTodoCertifications[].certificationInfo")
                                        .description("투두 인증 정보")
                                        .type(JsonFieldType.ARRAY),
                                fieldWithPath("data.dailyTodoCertifications[].certificationInfo[].id")
                                        .description("데일리 투두 id")
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.dailyTodoCertifications[].certificationInfo[].content")
                                        .description("데일리 투두 내용")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.dailyTodoCertifications[].certificationInfo[].status")
                                        .description("데일리 투두 상태")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.dailyTodoCertifications[].certificationInfo[].certificationContent")
                                        .description("데일리 투두 인증글 내용")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.dailyTodoCertifications[].certificationInfo[].certificationMediaUrl")
                                        .description("데일리 투두 인증글 이미지 URL")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("data.dailyTodoCertifications[].certificationInfo[].rejectReason")
                                        .description("데일리 투두 인증 노인정 사유")
                                        .optional()
                                        .type(JsonFieldType.STRING))));
    }
}
