package site.dogether.dailytodo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.dogether.common.config.web.resolver.Authentication;
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
@RequestMapping("/api/todos")
@RestController
public class DailyTodoController {

    private final DailyTodoService dailyTodoService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createDailyTodos(
        @Authentication String authenticationToken,
        @RequestBody final CreateDailyTodosRequest request
    ) {
        dailyTodoService.saveDailyTodo(authenticationToken, request.todos());
        return ResponseEntity.ok(ApiResponse.success(CREATE_DAILY_TODOS));
    }

    @PostMapping("/{todoId}/certify")
    public ResponseEntity<ApiResponse<Void>> certifyDailyTodo(
        @Authentication String authenticationToken,
        @PathVariable final Long todoId,
        @RequestBody final CertifyDailyTodoRequest request
    ) {
        dailyTodoService.certifyDailyTodo(authenticationToken, todoId, request.content(), request.mediaUrls());
        return ResponseEntity.ok(ApiResponse.success(CERTIFY_DAILY_TODO));
    }

    @GetMapping("/my/yesterday")
    public ResponseEntity<ApiResponse<GetYesterdayDailyTodosResponse>> getYesterdayDailyTodos(
        @Authentication String authenticationToken
    ) {
        final List<String> yesterdayDailyTodos = dailyTodoService.findYesterdayDailyTodos(authenticationToken);
        final GetYesterdayDailyTodosResponse response = new GetYesterdayDailyTodosResponse(yesterdayDailyTodos);
        return ResponseEntity.ok(ApiResponse.successWithData(GET_YESTERDAY_DAILY_TODOS, response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<GetMyDailyTodosResponse>> getMyDailyTodos(
        @Authentication String authenticationToken,
        @RequestParam final LocalDate date,
        @RequestParam(required = false) final String status
    ) {
        final FindMyDailyTodosConditionDto findMyDailyTodosConditionDto = FindMyDailyTodosConditionDto.of(authenticationToken, date, status);
        final List<DailyTodoAndDailyTodoCertificationDto> myDailyTodos = dailyTodoService.findMyDailyTodo(findMyDailyTodosConditionDto);

        return ResponseEntity.ok(ApiResponse.successWithData(
            GET_MY_DAILY_TODOS,
            GetMyDailyTodosResponse.of(myDailyTodos)
        ));
    }
}
