package site.dogether.dailytodo.service.dto;

import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;

import java.util.Optional;

public class DailyTodoAndDailyTodoCertificationDto {

    private final DailyTodo dailyTodo;
    private final DailyTodoCertification dailyTodoCertification;

    public static DailyTodoAndDailyTodoCertificationDto withoutDailyTodoCertification(final DailyTodo dailyTodo) {
        return new DailyTodoAndDailyTodoCertificationDto(dailyTodo, null);
    }

    public DailyTodoAndDailyTodoCertificationDto(
        final DailyTodo dailyTodo,
        final DailyTodoCertification dailyTodoCertification
    ) {
        this.dailyTodo = dailyTodo;
        this.dailyTodoCertification = dailyTodoCertification;
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
        if (dailyTodoCertification == null) {
            return Optional.empty();
        }

        return Optional.of(dailyTodoCertification.getMediaUrl());
    }

    public Optional<String> findRejectReason() {
        if (dailyTodoCertification == null || dailyTodo.getStatus() != DailyTodoStatus.REJECT) {
            return Optional.empty();
        }

        return dailyTodo.getRejectReason();
    }
}
