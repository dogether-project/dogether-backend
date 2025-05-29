package site.dogether.dailytodo.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import site.dogether.dailytodo.service.dto.DailyTodoDto;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public record GetMyDailyTodosResponse(List<Data> todos) {

    public static GetMyDailyTodosResponse of(List<DailyTodoDto> todos) {
        return todos.stream()
            .map(Data::from)
            .collect(collectingAndThen(toList(), GetMyDailyTodosResponse::new));
    }

    record Data(
        Long id,
        String content,
        String status,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String certificationContent,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String certificationMediaUrl,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String reviewFeedback
    ) {
        public static Data from(final DailyTodoDto dailyTodo) {
            return new Data(
                dailyTodo.getId(),
                dailyTodo.getContent(),
                dailyTodo.getStatus(),
                dailyTodo.findCertificationContent().orElse(null),
                dailyTodo.findCertificationMediaUrl().orElse(null),
                dailyTodo.findReviewFeedback().orElse(null)
            );
        }
    }
}
