package site.dogether.dailytodo.repository;

import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;

public record DailyTodoAndDailyTodoCertification(
    DailyTodo dailyTodo,
    DailyTodoCertification dailyTodoCertification
) {}
