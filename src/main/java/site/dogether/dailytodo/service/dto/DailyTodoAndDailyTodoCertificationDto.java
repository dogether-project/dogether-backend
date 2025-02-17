package site.dogether.dailytodo.service.dto;

import site.dogether.dailytodo.domain.DailyTodo;
import site.dogether.dailytodo.domain.DailyTodoStatus;
import site.dogether.dailytodocertification.domain.DailyTodoCertification;
import site.dogether.dailytodocertification.domain.DailyTodoCertificationMediaUrls;

import java.util.Optional;

public class DailyTodoAndDailyTodoCertificationDto {

    private final DailyTodo dailyTodo;
    private final DailyTodoCertification dailyTodoCertification;
    private final DailyTodoCertificationMediaUrls dailyTodoCertificationMediaUrls;

    public static DailyTodoAndDailyTodoCertificationDto of(final DailyTodo dailyTodo) {
        return new DailyTodoAndDailyTodoCertificationDto(dailyTodo, null, null);
    }

    public DailyTodoAndDailyTodoCertificationDto(
        final DailyTodo dailyTodo,
        final DailyTodoCertification dailyTodoCertification,
        final DailyTodoCertificationMediaUrls dailyTodoCertificationMediaUrls
    ) {
        this.dailyTodo = dailyTodo;
        this.dailyTodoCertification = dailyTodoCertification;
        this.dailyTodoCertificationMediaUrls = dailyTodoCertificationMediaUrls;
    }

    public Long getDailyTodoId() {
        return dailyTodo.getId();
    }

    public String getDailyTodoContent() {
        return dailyTodo.getContent();
    }

    public DailyTodoStatus getDailyTodoStatus() {
        return dailyTodo.getStatus();
    }

    public Optional<String> findDailyTodoCertificationContent() {
        if (dailyTodoCertification == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(dailyTodoCertification.getContent());
    }

    public Optional<String> findDailyTodoCertificationMediaUrl() {
        if (dailyTodoCertificationMediaUrls == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(dailyTodoCertificationMediaUrls.getValues().get(0));
    }

    public Optional<String> findRejectReason() {
        if (dailyTodoCertification == null || dailyTodo.getStatus() != DailyTodoStatus.REJECT) {
            return Optional.empty();
        }
        return dailyTodo.getRejectReason();
    }
}
