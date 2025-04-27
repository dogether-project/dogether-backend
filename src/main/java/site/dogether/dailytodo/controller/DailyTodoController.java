package site.dogether.dailytodo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.dailytodo.controller.request.CreateDailyTodosRequest;
import site.dogether.dailytodo.controller.response.GetChallengeMemberTodayTodoHistoryResponse;
import site.dogether.dailytodo.controller.response.GetMyDailyTodosResponse;
import site.dogether.dailytodo.controller.response.GetYesterdayDailyTodosResponse;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodo.service.DailyTodoService;
import site.dogether.dailytodo.service.dto.DailyTodoAndDailyTodoCertificationDto;
import site.dogether.dailytodo.service.dto.FindMyDailyTodosConditionDto;

import java.time.LocalDate;
import java.util.List;

import static site.dogether.dailytodo.controller.response.DailyTodoSuccessCode.*;

@RequiredArgsConstructor
@RequestMapping("/api/challenge-groups/{groupId}")
@RestController
public class DailyTodoController {

    private final DailyTodoService dailyTodoService;

    @PostMapping("/todos")
    public ResponseEntity<ApiResponse<Void>> createDailyTodos(
        @Authenticated Long memberId,
        @PathVariable Long groupId,
        @RequestBody final CreateDailyTodosRequest request
    ) {
        dailyTodoService.saveDailyTodo(memberId, request.todos());
        return ResponseEntity.ok(ApiResponse.success(CREATE_DAILY_TODOS));
    }

    @GetMapping("/my-yesterday-todos")
    public ResponseEntity<ApiResponse<GetYesterdayDailyTodosResponse>> getYesterdayDailyTodos(
        @Authenticated Long memberId,
        @PathVariable Long groupId
    ) {
        final List<String> yesterdayDailyTodos = dailyTodoService.findYesterdayDailyTodos(memberId);
        final GetYesterdayDailyTodosResponse response = new GetYesterdayDailyTodosResponse(yesterdayDailyTodos);
        return ResponseEntity.ok(ApiResponse.successWithData(GET_YESTERDAY_DAILY_TODOS, response));
    }

    @GetMapping("/my-todos")
    public ResponseEntity<ApiResponse<GetMyDailyTodosResponse>> getMyDailyTodosWithCertification(
        @Authenticated Long memberId,
        @PathVariable final Long groupId,
        @RequestParam final LocalDate date,
        @RequestParam(required = false) final String status
    ) {
        final FindMyDailyTodosConditionDto findMyDailyTodosConditionDto = FindMyDailyTodosConditionDto.of(memberId, date, status);
        final List<DailyTodoAndDailyTodoCertificationDto> myDailyTodos = dailyTodoService.findMyDailyTodo(findMyDailyTodosConditionDto);

        return ResponseEntity.ok(ApiResponse.successWithData(
            GET_MY_DAILY_TODOS,
            GetMyDailyTodosResponse.of(myDailyTodos)
        ));
    }

    @GetMapping("/challenge-group-members/{challengeGroupMemberId}/today-todo-history")
    public ResponseEntity<ApiResponse<GetChallengeMemberTodayTodoHistoryResponse>> getChallengeGroupMemberTodayTodoHistory(
        @Authenticated Long memberId,
        @PathVariable final Long groupId,
        @PathVariable final Long challengeGroupMemberId
    ) {
        return ResponseEntity.ok(ApiResponse.successWithData(
            GET_CHALLENGE_GROUP_MEMBER_TODAY_TODOS,
            new GetChallengeMemberTodayTodoHistoryResponse(
                3,
                List.of(
                    new GetChallengeMemberTodayTodoHistoryResponse.TodoData(1L, "치킨 먹기", DailyTodoStatus.CERTIFY_PENDING, null, null, true),
                    new GetChallengeMemberTodayTodoHistoryResponse.TodoData(2L, "재홍님 갈구기", DailyTodoStatus.CERTIFY_PENDING, null, null, true),
                    new GetChallengeMemberTodayTodoHistoryResponse.TodoData(3L, "치킨 먹기", DailyTodoStatus.REVIEW_PENDING, "개꿀맛 치킨 냠냠", "https://치킨.png", true),
                    new GetChallengeMemberTodayTodoHistoryResponse.TodoData(4L, "재홍님 갈구기", DailyTodoStatus.REVIEW_PENDING, "아 재홍님 그거 그렇게 하는거 아닌데", "https://갈굼1.png", false),
                    new GetChallengeMemberTodayTodoHistoryResponse.TodoData(5L, "재홍님 갈구기", DailyTodoStatus.APPROVE, "아 재홍님 그거 그렇게 하는거 아닌데", "https://갈굼1.png", false),
        new GetChallengeMemberTodayTodoHistoryResponse.TodoData(6L, "치킨 먹기", DailyTodoStatus.REJECT, "개꿀맛 치킨 냠냠", "https://치킨.png", false)
                )
            )
        ));
    }
}
