package site.dogether.dailytodo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.dailytodo.controller.request.CertifyDailyTodoRequest;
import site.dogether.dailytodo.controller.request.CreateDailyTodosRequest;
import site.dogether.dailytodo.controller.response.GetMyDailyTodosResponse;
import site.dogether.dailytodo.controller.response.GetYesterdayDailyTodosResponse;
import site.dogether.dailytodo.service.DailyTodoService;
import site.dogether.dailytodo.service.dto.DailyTodoAndDailyTodoCertificationDto;
import site.dogether.dailytodo.service.dto.FindMyDailyTodosConditionDto;

import java.time.LocalDate;
import java.util.List;

import static site.dogether.dailytodo.controller.response.DailyTodoSuccessCode.*;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class DailyTodoController {

    private final DailyTodoService dailyTodoService;

    @PostMapping("/todos")
    public ResponseEntity<ApiResponse<Void>> createDailyTodos(
        @Authenticated Long memberId,
        @RequestBody final CreateDailyTodosRequest request
    ) {
        dailyTodoService.saveDailyTodo(memberId, request.todos());
        return ResponseEntity.ok(ApiResponse.success(CREATE_DAILY_TODOS));
    }

    @PostMapping("/todos/{todoId}/certify")
    public ResponseEntity<ApiResponse<Void>> certifyDailyTodo(
        @Authenticated Long memberId,
        @PathVariable final Long todoId,
        @RequestBody final CertifyDailyTodoRequest request
    ) {
        dailyTodoService.certifyDailyTodo(memberId, todoId, request.content(), request.mediaUrls());
        return ResponseEntity.ok(ApiResponse.success(CERTIFY_DAILY_TODO));
    }

    @GetMapping("/challenge-groups/{groupId}/my-yesterday-todos")
    public ResponseEntity<ApiResponse<GetYesterdayDailyTodosResponse>> getYesterdayDailyTodos(
        @Authenticated Long memberId,
        @PathVariable Long groupId
    ) {
        final List<String> yesterdayDailyTodos = dailyTodoService.findYesterdayDailyTodos(memberId);
        final GetYesterdayDailyTodosResponse response = new GetYesterdayDailyTodosResponse(yesterdayDailyTodos);
        return ResponseEntity.ok(ApiResponse.successWithData(GET_YESTERDAY_DAILY_TODOS, response));
    }

    @GetMapping("/challenge-groups/{groupId}/my-todos")
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
}
