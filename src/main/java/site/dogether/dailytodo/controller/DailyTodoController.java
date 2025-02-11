package site.dogether.dailytodo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.dogether.common.config.web.resolver.Authentication;
import site.dogether.dailytodo.controller.request.CertifyDailyTodoRequest;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.dailytodo.controller.request.CreateDailyTodosRequest;
import site.dogether.dailytodo.controller.request.GetYesterdayDailyTodosResponse;
import site.dogether.dailytodo.service.DailyTodoService;

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
}
