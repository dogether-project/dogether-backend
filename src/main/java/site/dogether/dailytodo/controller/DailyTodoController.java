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
import site.dogether.common.controller.dto.response.ApiResponse;
import site.dogether.dailytodo.controller.v1.dto.request.CreateDailyTodosApiRequestV1;
import site.dogether.dailytodo.controller.v1.dto.response.GetChallengeGroupMemberTodayTodoHistoryApiResponseV1;
import site.dogether.dailytodo.controller.v1.dto.response.GetMyDailyTodosApiResponseV1;
import site.dogether.dailytodo.controller.v1.dto.response.GetYesterdayDailyTodosApiResponseV1;
import site.dogether.dailytodo.service.DailyTodoService;
import site.dogether.dailytodo.service.dto.DailyTodoDto;
import site.dogether.dailytodo.service.dto.FindMyDailyTodosConditionDto;
import site.dogether.dailytodohistory.service.DailyTodoHistoryService;
import site.dogether.dailytodohistory.service.dto.FindTargetMemberTodayTodoHistoriesDto;

import java.time.LocalDate;
import java.util.List;

import static site.dogether.common.controller.dto.response.ApiResponse.success;

@RequiredArgsConstructor
@RestController
public class DailyTodoController {

    private final DailyTodoService dailyTodoService;
    private final DailyTodoHistoryService dailyTodoHistoryService;

    @PostMapping("/api/challenge-groups/{groupId}/todos")
    public ResponseEntity<ApiResponse<Void>> createDailyTodos(
        @Authenticated final Long memberId,
        @PathVariable final Long groupId,
        @RequestBody final CreateDailyTodosApiRequestV1 request
    ) {
        dailyTodoService.saveDailyTodos(memberId, groupId, request.todos());
        return ResponseEntity.ok(success());
    }

    @GetMapping("/api/challenge-groups/{groupId}/my-yesterday-todos")
    public ResponseEntity<ApiResponse<GetYesterdayDailyTodosApiResponseV1>> getYesterdayDailyTodos(
        @Authenticated final Long memberId,
        @PathVariable final Long groupId
    ) {
        final List<String> yesterdayDailyTodos = dailyTodoService.findYesterdayDailyTodos(memberId, groupId);
        final GetYesterdayDailyTodosApiResponseV1 response = new GetYesterdayDailyTodosApiResponseV1(yesterdayDailyTodos);
        return ResponseEntity.ok(success(response));
    }

    @GetMapping("/api/challenge-groups/{groupId}/my-todos")
    public ResponseEntity<ApiResponse<GetMyDailyTodosApiResponseV1>> getMyDailyTodos(
        @Authenticated final Long memberId,
        @PathVariable final Long groupId,
        @RequestParam final LocalDate date,
        @RequestParam(required = false) final String status
    ) {
        final FindMyDailyTodosConditionDto findMyDailyTodosConditionDto = new FindMyDailyTodosConditionDto(memberId, groupId, date, status);
        final List<DailyTodoDto> myDailyTodos = dailyTodoService.findMyDailyTodos(findMyDailyTodosConditionDto);
        return ResponseEntity.ok(success(GetMyDailyTodosApiResponseV1.of(myDailyTodos)));
    }

    @GetMapping("/api/challenge-groups/{groupId}/challenge-group-members/{targetMemberId}/today-todo-history")
    public ResponseEntity<ApiResponse<GetChallengeGroupMemberTodayTodoHistoryApiResponseV1>> getChallengeGroupMemberTodayTodoHistory(
        @Authenticated final Long memberId,
        @PathVariable final Long groupId,
        @PathVariable final Long targetMemberId
    ) {
        final FindTargetMemberTodayTodoHistoriesDto targetMemberTodayTodoHistories = dailyTodoHistoryService.findAllTodayTodoHistories(memberId, groupId, targetMemberId);
        final GetChallengeGroupMemberTodayTodoHistoryApiResponseV1 response = GetChallengeGroupMemberTodayTodoHistoryApiResponseV1.from(targetMemberTodayTodoHistories);
        return ResponseEntity.ok(success(response));
    }

    @PostMapping("/api/todo-history/{todoHistoryId}")
    public ResponseEntity<ApiResponse<Void>> markTodoHistoryAsRead(
        @Authenticated final Long memberId,
        @PathVariable final Long todoHistoryId
    ) {
        dailyTodoHistoryService.saveDailyTodoHistoryRead(memberId, todoHistoryId);
        return ResponseEntity.ok(success());
    }
}
