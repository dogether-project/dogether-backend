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
import site.dogether.memberactivity.service.MemberActivityService;

import java.util.List;

import static site.dogether.memberactivity.controller.response.MemberActivitySuccessCode.GET_GROUP_ACTIVITY_STAT;
import static site.dogether.memberactivity.controller.response.MemberActivitySuccessCode.GET_MEMBER_ALL_STATS;

@RequiredArgsConstructor
@RequestMapping("/api/my")
@RestController
public class MemberActivityController {

    private final MemberActivityService memberActivityService;

    @GetMapping("/groups/{groupId}/activity")
    public ResponseEntity<ApiResponse<GetGroupActivityStatResponse>> getGroupActivityStat(
            @Authenticated final Long memberId, @PathVariable final Long groupId
    ) {
        final GetGroupActivityStatResponse groupActivityStat = memberActivityService.getGroupActivityStat(memberId, groupId);

        return ResponseEntity.ok(ApiResponse.successWithData(GET_GROUP_ACTIVITY_STAT, groupActivityStat));
    }

    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<GetMemberAllStatsResponse>> getMemberAllStats(
            @Authenticated final Long memberId,
            @RequestParam final String sort,
            @RequestParam(required = false) final String status
    ) {
        GetMemberAllStatsResponse.DailyTodoStats stats = new GetMemberAllStatsResponse.DailyTodoStats(
                5,
                3,
                2
        );

        List<Object> dailyTodoCertifications = null;

        if(sort.equals("TODO_COMPLETED_AT")) {
            dailyTodoCertifications = List.of(
                    new GetMemberAllStatsResponse.CertificationsSortByTodoCompletedAt(
                            "2025.05.01",
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
                    new GetMemberAllStatsResponse.CertificationsSortByTodoCompletedAt(
                            "2025.05.02",
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
        }
        else if (sort.equals("GROUP_CREATED_AT")) {
            dailyTodoCertifications = List.of(
                    new GetMemberAllStatsResponse.CertificationsSortByGroupCreatedAt(
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
                    new GetMemberAllStatsResponse.CertificationsSortByGroupCreatedAt(
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
        }

        GetMemberAllStatsResponse response = new GetMemberAllStatsResponse(stats, dailyTodoCertifications);

        return ResponseEntity.ok(ApiResponse.successWithData(GET_MEMBER_ALL_STATS, response));
    }
}
