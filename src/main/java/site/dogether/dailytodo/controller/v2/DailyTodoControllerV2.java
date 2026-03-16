package site.dogether.dailytodo.controller.v2;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.dto.response.ApiResponse;
import site.dogether.dailytodo.controller.v2.dto.response.GetChallengeGroupMemberTodayTodoHistoryApiResponseV2;
import site.dogether.dailytodo.controller.v2.dto.response.GetMyDailyTodosApiResponseV2;
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
public class DailyTodoControllerV2 {

    private final DailyTodoService dailyTodoService;
    private final DailyTodoHistoryService dailyTodoHistoryService;

    @GetMapping("/api/v2/challenge-groups/{groupId}/my-todos")
    public ResponseEntity<ApiResponse<GetMyDailyTodosApiResponseV2>> getMyDailyTodos(
        @Authenticated final Long memberId,
        @PathVariable final Long groupId,
        @RequestParam final LocalDate date,
        @RequestParam(required = false) final String status
    ) {
        final FindMyDailyTodosConditionDto findMyDailyTodosConditionDto = new FindMyDailyTodosConditionDto(memberId, groupId, date, status);
        final List<DailyTodoDto> myDailyTodos = dailyTodoService.findMyDailyTodos(findMyDailyTodosConditionDto);
        return ResponseEntity.ok(success(GetMyDailyTodosApiResponseV2.of(myDailyTodos)));
    }

    @GetMapping("/api/v2/challenge-groups/{groupId}/challenge-group-members/{targetMemberId}/today-todo-history")
    public ResponseEntity<ApiResponse<GetChallengeGroupMemberTodayTodoHistoryApiResponseV2>> getChallengeGroupMemberTodayTodoHistory(
        @Authenticated final Long memberId,
        @PathVariable final Long groupId,
        @PathVariable final Long targetMemberId
    ) {
        final FindTargetMemberTodayTodoHistoriesDto targetMemberTodayTodoHistories = dailyTodoHistoryService.findAllTodayTodoHistories(memberId, groupId, targetMemberId);
        final GetChallengeGroupMemberTodayTodoHistoryApiResponseV2 response = GetChallengeGroupMemberTodayTodoHistoryApiResponseV2.from(targetMemberTodayTodoHistories);
        return ResponseEntity.ok(success(response));
    }
}
