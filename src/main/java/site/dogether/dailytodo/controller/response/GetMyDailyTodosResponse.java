package site.dogether.dailytodo.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodo.service.dto.DailyTodoAndDailyTodoCertificationDto;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public record GetMyDailyTodosResponse(List<Data> todos) {

    public static GetMyDailyTodosResponse of(List<DailyTodoAndDailyTodoCertificationDto> todos) {
        return todos.stream()
            .map(Data::from)
            .collect(collectingAndThen(toList(), GetMyDailyTodosResponse::new));
    }
}

record Data(
    Long id,
    String content,
    DailyTodoStatus status,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String certificationContent,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String certificationMediaUrl,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String reviewFeedback
) {
    public static Data from(final DailyTodoAndDailyTodoCertificationDto dailyTodo) {
        return new Data(
            dailyTodo.getDailyTodoId(),
            dailyTodo.getDailyTodoContent(),
            dailyTodo.getDailyTodoStatus(),
            dailyTodo.findDailyTodoCertificationContent().orElse(null),
            dailyTodo.findDailyTodoCertificationMediaUrl().orElse(null),
            dailyTodo.findRejectReason().orElse(null)
        );
    }
}
