package site.dogether.memberactivity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.memberactivity.controller.response.GetGroupActivityStatResponse;
import site.dogether.memberactivity.controller.response.GetMemberAllStatsResponse;

import java.util.List;

import static site.dogether.memberactivity.controller.response.MemberActivitySuccessCode.GET_GROUP_ACTIVITY_STAT;
import static site.dogether.memberactivity.controller.response.MemberActivitySuccessCode.GET_MEMBER_ALL_STATS;

@RequiredArgsConstructor
@RequestMapping("/api/my")
@RestController
public class MemberActivityController {

    @GetMapping("/groups/{groupId}/activity")
    public ResponseEntity<ApiResponse<GetGroupActivityStatResponse>> getGroupActivityStat(
            @Authenticated final Long memberId, @PathVariable final Long groupId
    ) {
        GetGroupActivityStatResponse.ChallengeGroupInfoResponse groupInfo = new GetGroupActivityStatResponse.ChallengeGroupInfoResponse("그로밋과 함께하는 챌린지", 10, 6, "123456", "25.02.22");

        List<GetGroupActivityStatResponse.CertificationPeriodResponse> certificationPeriods = List.of(
                new GetGroupActivityStatResponse.CertificationPeriodResponse(1, 8, 2, 25),
                new GetGroupActivityStatResponse.CertificationPeriodResponse(2, 6, 3, 50),
                new GetGroupActivityStatResponse.CertificationPeriodResponse(3, 3, 3, 100)
        );

        GetGroupActivityStatResponse.RankingResponse ranking = new GetGroupActivityStatResponse.RankingResponse(10, 3);
        GetGroupActivityStatResponse.MemberStatsResponse stats = new GetGroupActivityStatResponse.MemberStatsResponse(123, 123, 123);

        GetGroupActivityStatResponse response = new GetGroupActivityStatResponse(
                groupInfo,
                certificationPeriods,
                ranking,
                stats
        );

        return ResponseEntity.ok(ApiResponse.successWithData(GET_GROUP_ACTIVITY_STAT, response));
    }

    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<GetMemberAllStatsResponse>> getMemberAllStats(
            @Authenticated final Long memberId,
            @RequestParam(name = "sort", defaultValue = "todo-completed-at") final String sort,
            @RequestParam final String status
    ) {
        GetMemberAllStatsResponse.DailyTodoStats stats = new GetMemberAllStatsResponse.DailyTodoStats(
                5,
                3,
                2
        );

        List<GetMemberAllStatsResponse.DailyTodoCertifications> certifications;

        if(sort.equals("todo-completed-at")) {
            certifications = List.of(
                    new GetMemberAllStatsResponse.DailyTodoCertifications(
                            1L,
                            null,
                            "2025.04.30",
                            "운동 하기",
                            "APPROVE",
                            "운동 개조짐 ㅋㅋㅋㅋ",
                            "운동 조지는 짤.png",
                            null
                    ),
                    new GetMemberAllStatsResponse.DailyTodoCertifications(
                            2L,
                            null,
                            "2025.04.31",
                            "인강 듣기",
                            "APPROVE",
                            "인강 진짜 열심히 들었습니다. ㅎ",
                            "인강 달리는 짤.png",
                            null
                    ),
                    new GetMemberAllStatsResponse.DailyTodoCertifications(
                            3L,
                            null,
                            "2025.04.31",
                            "두게더 API 구현",
                            "APPROVE",
                            "API 좀 잘 만든듯 ㅋ",
                            "API 명세짤.png",
                            null
                    )
            );
        }
        else if(sort.equals("group-created-at")) {
            certifications = List.of(
                    new GetMemberAllStatsResponse.DailyTodoCertifications(
                            1L,
                            "스쿼트 챌린지",
                            null,
                            "운동 하기",
                            "REJECT",
                            "운동 개조짐 ㅋㅋㅋㅋ",
                            "운동 조지는 짤.png",
                            "에이 이건 운동 아니지"
                    ),
                    new GetMemberAllStatsResponse.DailyTodoCertifications(
                            2L,
                            "TIL 챌린지",
                            null,
                            "인강 듣기",
                            "REJECT",
                            "인강 진짜 열심히 들었습니다. ㅎ",
                            "인강 달리는 짤.png",
                            "우리 오늘 인강 듣는날 아닌데?"
                    ),
                    new GetMemberAllStatsResponse.DailyTodoCertifications(
                            3L,
                            "두게더 개발단",
                            null,
                            "두게더 API 구현",
                            "REJECT",
                            "API 좀 잘 만든듯 ㅋ",
                            "API 명세짤.png",
                            "아 별론데?"
                    )
            );
        }
        else {
            certifications = List.of(
                    new GetMemberAllStatsResponse.DailyTodoCertifications(
                            1L,
                            null,
                            null,
                            "운동 하기",
                            "REVIEW_PENDING",
                            "운동 개조짐 ㅋㅋㅋㅋ",
                            "운동 조지는 짤.png",
                            null
                    ),
                    new GetMemberAllStatsResponse.DailyTodoCertifications(
                            2L,
                            null,
                            null,
                            "인강 듣기",
                            "REVIEW_PENDING",
                            "인강 진짜 열심히 들었습니다. ㅎ",
                            "인강 달리는 짤.png",
                            null
                    ),
                    new GetMemberAllStatsResponse.DailyTodoCertifications(
                            3L,
                            null,
                            null,
                            "두게더 API 구현",
                            "REVIEW_PENDING",
                            "API 좀 잘 만든듯 ㅋ",
                            "API 명세짤.png",
                            null
                    )
            );
        }

        GetMemberAllStatsResponse response = new GetMemberAllStatsResponse(stats, certifications);

        return ResponseEntity.ok(ApiResponse.successWithData(GET_MEMBER_ALL_STATS, response));
    }
}
