package site.dogether.dailytodo.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DailyTodoStatus {

    CERTIFY_PENDING("인증 대기"),
    REVIEW_PENDING("검사 대기"),
    APPROVE("인정"),
    REJECT("노인정"),
    ;

    private final String description;
}
