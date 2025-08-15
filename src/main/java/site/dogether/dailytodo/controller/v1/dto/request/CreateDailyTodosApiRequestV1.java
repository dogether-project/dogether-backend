package site.dogether.dailytodo.controller.v1.dto.request;

import java.util.List;

public record CreateDailyTodosApiRequestV1(List<String> todos) {
}
