package site.dogether.dailytodo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.dailytodo.controller.request.CreateDailyTodosRequest;
import site.dogether.dailytodo.controller.response.GetChallengeGroupMemberTodayTodoHistoryResponse;
import site.dogether.dailytodo.controller.response.GetMyDailyTodosResponse;
import site.dogether.dailytodo.controller.response.GetYesterdayDailyTodosResponse;
import site.dogether.dailytodo.service.DailyTodoService;
import site.dogether.dailytodo.service.dto.DailyTodoDto;
import site.dogether.dailytodo.service.dto.FindMyDailyTodosConditionDto;
import site.dogether.dailytodohistory.service.DailyTodoHistoryService;
import site.dogether.dailytodohistory.service.dto.FindTargetMemberTodayTodoHistoriesDto;

import java.time.LocalDate;
import java.util.List;

import static site.dogether.dailytodo.controller.response.DailyTodoSuccessCode.*;

@RequiredArgsConstructor
@RestController
public class DailyTodoController {

    private final DailyTodoService dailyTodoService;
    private final DailyTodoHistoryService dailyTodoHistoryService;

    @PostMapping("/api/challenge-groups/{groupId}/todos")
    public ResponseEntity<ApiResponse<Void>> createDailyTodos(
        @Authenticated final Long memberId,
        @PathVariable final Long groupId,
        @RequestBody final CreateDailyTodosRequest request
    ) {
        dailyTodoService.saveDailyTodos(memberId, groupId, request.todos());
        return ResponseEntity.ok(ApiResponse.success(CREATE_DAILY_TODOS));
    }

    @GetMapping("/api/challenge-groups/{groupId}/my-yesterday-todos")
    public ResponseEntity<ApiResponse<GetYesterdayDailyTodosResponse>> getYesterdayDailyTodos(
        @Authenticated final Long memberId,
        @PathVariable final Long groupId
    ) {
        final List<String> yesterdayDailyTodos = dailyTodoService.findYesterdayDailyTodos(memberId, groupId);
        final GetYesterdayDailyTodosResponse response = new GetYesterdayDailyTodosResponse(yesterdayDailyTodos);
        return ResponseEntity.ok(ApiResponse.successWithData(GET_YESTERDAY_DAILY_TODOS, response));
    }

    @GetMapping("/api/challenge-groups/{groupId}/my-todos")
    public ResponseEntity<ApiResponse<GetMyDailyTodosResponse>> getMyDailyTodos(
        @Authenticated final Long memberId,
        @PathVariable final Long groupId,
        @RequestParam final LocalDate date,
        @RequestParam(required = false) final String status
    ) {
        final FindMyDailyTodosConditionDto findMyDailyTodosConditionDto = new FindMyDailyTodosConditionDto(memberId, groupId, date, status);
        final List<DailyTodoDto> myDailyTodos = dailyTodoService.findMyDailyTodos(findMyDailyTodosConditionDto);
        return ResponseEntity.ok(ApiResponse.successWithData(
            GET_MY_DAILY_TODOS,
            GetMyDailyTodosResponse.of(myDailyTodos)
        ));
    }

    @GetMapping("/api/challenge-groups/{groupId}/challenge-group-members/{targetMemberId}/today-todo-history")
    public ResponseEntity<ApiResponse<GetChallengeGroupMemberTodayTodoHistoryResponse>> getChallengeGroupMemberTodayTodoHistory(
        @Authenticated final Long memberId,
        @PathVariable final Long groupId,
        @PathVariable final Long targetMemberId
    ) {
        final FindTargetMemberTodayTodoHistoriesDto targetMemberTodayTodoHistories = dailyTodoHistoryService.findAllTodayTodoHistories(memberId, groupId, targetMemberId);
        final GetChallengeGroupMemberTodayTodoHistoryResponse response = GetChallengeGroupMemberTodayTodoHistoryResponse.from(targetMemberTodayTodoHistories);
        return ResponseEntity.ok(ApiResponse.successWithData(GET_CHALLENGE_GROUP_MEMBER_TODAY_TODO_HISTORY, response));
    }

    @PostMapping("/api/todo-history/{todoHistoryId}")
    public ResponseEntity<ApiResponse<Void>> markTodoHistoryAsRead(
        @Authenticated final Long memberId,
        @PathVariable final Long todoHistoryId
    ) {
        dailyTodoHistoryService.saveDailyTodoHistoryRead(memberId, todoHistoryId);
        return ResponseEntity.ok(ApiResponse.success(MARK_TODO_HISTORY_AS_READ));
    }
}
