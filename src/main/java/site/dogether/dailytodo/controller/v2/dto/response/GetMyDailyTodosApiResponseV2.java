package site.dogether.dailytodo.controller.v2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import site.dogether.dailytodo.service.dto.DailyTodoDto;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public record GetMyDailyTodosApiResponseV2(List<Data> todos) {

    public static GetMyDailyTodosApiResponseV2 of(List<DailyTodoDto> todos) {
        return todos.stream()
            .map(Data::from)
            .collect(collectingAndThen(toList(), GetMyDailyTodosApiResponseV2::new));
    }

    record Data(
        Long id,
        String content,
        String status,
        boolean canRequestCertificationReview,
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
                dailyTodo.canRequestCertificationReview(),
                dailyTodo.findCertificationContent().orElse(null),
                dailyTodo.findCertificationMediaUrl().orElse(null),
                dailyTodo.findReviewFeedback().orElse(null)
            );
        }
    }
}
