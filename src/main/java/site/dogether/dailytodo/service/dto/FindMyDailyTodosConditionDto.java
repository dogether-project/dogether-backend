package site.dogether.dailytodo.service.dto;

import lombok.Getter;
import site.dogether.dailytodo.domain.DailyTodoStatus;

import java.time.LocalDate;
import java.util.Optional;

public class FindMyDailyTodosConditionDto {

    @Getter
    private final Long memberId;
    @Getter
    private final LocalDate createdAt;
    private final DailyTodoStatus status;

    public static FindMyDailyTodosConditionDto of(
        final Long memberId,
        final LocalDate createdAt,
        final String status
    ) {
        return new FindMyDailyTodosConditionDto(
            memberId,
            createdAt,
            convertDailyTodoStatus(status)
        );
    }

    private static DailyTodoStatus convertDailyTodoStatus(final String dailyTodoStatusValue) {
        if (dailyTodoStatusValue == null) {
            return null;
        }
        return DailyTodoStatus.valueOf(dailyTodoStatusValue);
    }

    public FindMyDailyTodosConditionDto(
        final Long memberId,
        final LocalDate createdAt,
        final DailyTodoStatus status
    ) {
        this.memberId = memberId;
        this.createdAt = createdAt;
        this.status = status;
    }

    public Optional<DailyTodoStatus> getDailyTodoStatus() {
        return Optional.ofNullable(status);
    }
}
