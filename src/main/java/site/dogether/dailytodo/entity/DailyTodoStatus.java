package site.dogether.dailytodo.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DailyTodoStatus {

    CERTIFY_PENDING,
    CERTIFY_COMPLETED
    ;
}
