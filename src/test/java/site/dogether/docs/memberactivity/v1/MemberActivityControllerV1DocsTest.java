package site.dogether.docs.memberactivity.v1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.docs.util.RestDocsSupport;
import site.dogether.memberactivity.controller.v1.MemberActivityControllerV1;
import site.dogether.memberactivity.service.MemberActivityService;
import site.dogether.memberactivity.service.dto.CertificationPeriodDto;
import site.dogether.memberactivity.service.dto.CertificationsGroupedByCertificatedAtDto;
import site.dogether.memberactivity.service.dto.CertificationsGroupedByGroupCreatedAtDto;
import site.dogether.memberactivity.service.dto.ChallengeGroupInfoDto;
import site.dogether.memberactivity.service.dto.DailyTodoCertificationActivityDto;
import site.dogether.memberactivity.service.dto.DailyTodoCertificationInfoDto;
import site.dogether.memberactivity.service.dto.FindMyProfileDto;
import site.dogether.memberactivity.service.dto.MyCertificationStatsDto;
import site.dogether.memberactivity.service.dto.MyCertificationStatsInChallengeGroupDto;
import site.dogether.memberactivity.service.dto.MyRankInChallengeGroupDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("사용자 활동 V1 API 문서화 테스트")
class MemberActivityControllerV1DocsTest extends RestDocsSupport {

    private final MemberActivityService memberActivityService = mock(MemberActivityService.class);

    @Override
    protected Object initController() {
        return new MemberActivityControllerV1(memberActivityService);
    }

    @DisplayName("[V1] 참여중인 특정 챌린지 그룹 활동 통계 조회 API")
    @Test
    void getGroupActivityStatV1() throws Exception {
        final ChallengeGroupInfoDto challengeGroupInfo = new ChallengeGroupInfoDto(
            "그로밋과 함께하는 챌린지",
            10,
            6,
            "123456",
            "25.02.22"
        );

        final List<CertificationPeriodDto> certificationPeriods = List.of(
            new CertificationPeriodDto(1, 8, 2, 25),
            new CertificationPeriodDto(2, 6, 3, 50),
            new CertificationPeriodDto(3, 6, 3, 50),
            new CertificationPeriodDto(4, 3, 3, 100)
        );

        final MyRankInChallengeGroupDto myRankInChallengeGroup = new MyRankInChallengeGroupDto(10, 3);
        final MyCertificationStatsInChallengeGroupDto myChallengeGroupStats = new MyCertificationStatsInChallengeGroupDto(123, 123, 123);

        given(memberActivityService.getChallengeGroupInfo(any(), any()))
            .willReturn(challengeGroupInfo);

        given(memberActivityService.getCertificationPeriods(any(), any()))
            .willReturn(certificationPeriods);

        given(memberActivityService.getMyRankInChallengeGroup(any(), any()))
            .willReturn(myRankInChallengeGroup);

        given(memberActivityService.getMyCertificationStatsInChallengeGroup(any(), any()))
            .willReturn(myChallengeGroupStats);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/my//groups/{groupId}/activity", 1)
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

    @DisplayName("[V1] 사용자의 활동 통계 및 작성한 인증 목록 전체 조회 API (투두 완료일 순)")
    @Test
    void getMemberAllStatsSortedByTodoCompletedAtV1() throws Exception {
        final MyCertificationStatsDto stats = new MyCertificationStatsDto(
                5,
                3,
                2
        );

        final Slice<DailyTodoCertification> slice = new SliceImpl<>(List.of(), PageRequest.of(0, 50), false);

        final List<CertificationsGroupedByCertificatedAtDto> certificationsGroupedByCertificatedAt = List.of(
                new CertificationsGroupedByCertificatedAtDto(
                        "2025.05.01",
                        List.of(
                                new DailyTodoCertificationInfoDto(
                                        1L,
                                        "운동 하기",
                                        "APPROVE",
                                        "운동 개조짐 ㅋㅋㅋㅋ",
                                        "운동 조지는 짤.png",
                                        "저도 같이 해요..."
                                )
                        )
                ),
                new CertificationsGroupedByCertificatedAtDto(
                        "2025.05.02",
                        List.of(
                                new DailyTodoCertificationInfoDto(
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

        given(memberActivityService.getMyCertificationStats(any()))
                .willReturn(stats);

        given(memberActivityService.getCertificationsByStatus(any(), any(), any()))
            .willReturn(slice);

        given(memberActivityService.certificationsGroupedByCertificatedAt(any()))
            .willReturn(certificationsGroupedByCertificatedAt);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/my/activity")
                    .param("sortBy", "TODO_COMPLETED_AT")
                    .param("status", "APPROVE")
                    .param("page", "0")
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
                        .description("페이지 번호")
                        .attributes(constraints("0부터 시작"))),
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

    @DisplayName("[V1] 사용자의 활동 통계 및 작성한 인증 목록 전체 조회 API (그룹 생성일 순)")
    @Test
    void getMemberAllStatsSortedByGroupCreatedAtV1() throws Exception {
        final MyCertificationStatsDto stats = new MyCertificationStatsDto(
                5,
                3,
                2
        );

        final Slice<DailyTodoCertification> slice = new SliceImpl<>(List.of(), PageRequest.of(0, 50), false);

        final List<CertificationsGroupedByGroupCreatedAtDto> certificationsGroupedByGroupCreatedAt = List.of(
                new CertificationsGroupedByGroupCreatedAtDto(
                        "스쿼트 챌린지",
                        List.of(
                                new DailyTodoCertificationInfoDto(
                                        1L,
                                        "운동 하기",
                                        "REJECT",
                                        "운동 개조짐 ㅋㅋㅋㅋ",
                                        "운동 조지는 짤.png",
                                        "에이 이건 운동 아니지"
                                )
                        )
                ),
                new CertificationsGroupedByGroupCreatedAtDto(
                        "TIL 챌린지",
                        List.of(
                                new DailyTodoCertificationInfoDto(
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

        given(memberActivityService.getMyCertificationStats(any()))
            .willReturn(stats);

        given(memberActivityService.getCertificationsByStatus(any(), any(), any()))
            .willReturn(slice);

        given(memberActivityService.certificationsGroupedByGroupCreatedAt(any()))
            .willReturn(certificationsGroupedByGroupCreatedAt);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/my/activity")
                                .param("sortBy", "GROUP_CREATED_AT")
                                .param("status", "REJECT")
                                .param("page", "0")
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
                                    .description("페이지 번호")
                                    .attributes(constraints("0부터 시작"))),
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

    @DisplayName("[V1] 사용자의 인증 목록 전체 조회 API (페이징 X)")
    @Test
    void getMyGroupCertificationsV1() throws Exception {
        final List<DailyTodoCertificationActivityDto> certifications = List.of(
            new DailyTodoCertificationActivityDto(
                1L,
                "운동 하기",
                "REJECT",
                false,
                "운동 개조짐 ㅋㅋㅋㅋ",
                "운동 조지는 짤.png",
                "에이 이건 운동 아니지"
            ),
            new DailyTodoCertificationActivityDto(
                2L,
                "인강 듣기",
                "REJECT",
                false,
                "인강 진짜 열심히 들었습니다. ㅎ",
                "인강 달리는 짤.png",
                "우리 오늘 인강 듣는날 아닌데?"
            )
        );

        given(memberActivityService.getMyGroupCertificationsByCertificatedAt(any(), any(), any()))
            .willReturn(certifications);

        given(memberActivityService.getMyGroupCertificationsByGroupCreatedAt(any(), any(), any()))
            .willReturn(certifications);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/my/activity/todos/{todoId}/group-certifications", 1)
                    .param("sortBy", "GROUP_CREATED_AT")
                    .param("status", "REJECT")
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
                        .attributes(options("REVIEW_PENDING(검사 대기)", "APPROVE(인정)", "REJECT(노인정)"))
                ),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.certifications[].id")
                        .description("데일리 투두 id")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.certifications[].content")
                        .description("데일리 투두 내용")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.certifications[].status")
                        .description("데일리 투두 상태")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.certifications[].canRequestCertificationReview")
                        .description("데일리 투두 인증 검사 요청 가능 여부")
                        .type(JsonFieldType.BOOLEAN),
                    fieldWithPath("data.certifications[].certificationContent")
                        .description("데일리 투두 인증글 내용")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.certifications[].certificationMediaUrl")
                        .description("데일리 투두 인증글 이미지 URL")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.certifications[].reviewFeedback")
                        .description("데일리 투두 인증 검사 피드백")
                        .type(JsonFieldType.STRING)
                )));
    }

    @DisplayName("[V1] 사용자 프로필 조회 API")
    @Test
    void getMyProfileV1() throws Exception {

        FindMyProfileDto myProfileDto = new FindMyProfileDto(
            "그로밋",
            "그로밋의 셀카.png"
        );

        given(memberActivityService.getMyProfile(any()))
            .willReturn(myProfileDto);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/my/profile")
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
