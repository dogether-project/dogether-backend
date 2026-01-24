package site.dogether.docs.memberactivity.v2;

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
import site.dogether.memberactivity.controller.v2.MemberActivityControllerV2;
import site.dogether.memberactivity.service.MemberActivityService;
import site.dogether.memberactivity.service.dto.CertificationPeriodDto;
import site.dogether.memberactivity.service.dto.ChallengeGroupInfoDto;
import site.dogether.memberactivity.service.dto.DailyTodoCertificationInfoDto;
import site.dogether.memberactivity.service.dto.GroupedCertificationsDto;
import site.dogether.memberactivity.service.dto.GroupedCertificationsResultDto;
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

@DisplayName("사용자 활동 V2 API 문서화 테스트")
class MemberActivityControllerV2DocsTest extends RestDocsSupport {

    private final MemberActivityService memberActivityService = mock(MemberActivityService.class);

    @Override
    protected Object initController() {
        return new MemberActivityControllerV2(memberActivityService);
    }

    @DisplayName("[V2] 참여중인 특정 챌린지 그룹 활동 요약 조회 API")
    @Test
    void getMyChallengeGroupActivitySummaryV2() throws Exception {
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

        given(memberActivityService.getChallengeGroupInfo(any(), any()))
            .willReturn(challengeGroupInfo);

        given(memberActivityService.getCertificationPeriods(any(), any()))
            .willReturn(certificationPeriods);

        given(memberActivityService.getMyRankInChallengeGroup(any(), any()))
            .willReturn(myRankInChallengeGroup);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v2/my/groups/{groupId}/activity-summary", 1)
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
                        .type(JsonFieldType.NUMBER))));
    }

    @DisplayName("[V2] 사용자 인증 목록 전체 조회 API")
    @Test
    void getMyCertificationsV2() throws Exception {
        final Slice<DailyTodoCertification> slice = new SliceImpl<>(List.of(), PageRequest.of(0, 50), false);

        final List<GroupedCertificationsDto> certifications = List.of(
                new GroupedCertificationsDto(
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
                new GroupedCertificationsDto(
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

        final GroupedCertificationsResultDto groupedCertificationsResult = new GroupedCertificationsResultDto(certifications, slice);

        given(memberActivityService.getCertifications(any(), any(), any(), any()))
            .willReturn(groupedCertificationsResult);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v2/my/certifications")
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
                        .attributes(options("CERTIFICATED_AT(투두 인증일 순)", "GROUP_CREATED_AT(그룹 생성일 순)")),
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
                    fieldWithPath("data.certifications")
                        .description("인증한 투두 목록")
                        .optional()
                        .type(JsonFieldType.ARRAY),
                    fieldWithPath("data.certifications[].groupedBy")
                        .description("그룹핑 기준 값")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.certifications[].certificationInfo")
                        .description("투두 인증 정보")
                        .type(JsonFieldType.ARRAY),
                    fieldWithPath("data.certifications[].certificationInfo[].id")
                        .description("데일리 투두 id")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.certifications[].certificationInfo[].content")
                        .description("데일리 투두 내용")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.certifications[].certificationInfo[].status")
                        .description("데일리 투두 상태")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.certifications[].certificationInfo[].certificationContent")
                        .description("데일리 투두 인증글 내용")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.certifications[].certificationInfo[].certificationMediaUrl")
                        .description("데일리 투두 인증글 이미지 URL")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.certifications[].certificationInfo[].reviewFeedback")
                        .description("데일리 투두 인증 검사 피드백")
                        .optional()
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.pageInfo.recentPageNumber")
                        .description("현재 페이지 번호")
                        .type(JsonFieldType.NUMBER),
                    fieldWithPath("data.pageInfo.hasNext")
                        .description("다음 페이지 존재 여부")
                        .type(JsonFieldType.BOOLEAN)
                )));
    }
}
