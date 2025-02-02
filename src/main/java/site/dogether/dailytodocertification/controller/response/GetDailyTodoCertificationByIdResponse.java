package site.dogether.dailytodocertification.controller.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record GetDailyTodoCertificationByIdResponse(@JsonUnwrapped DailyTodoCertificationResponse dailyTodoCertification) {
}
