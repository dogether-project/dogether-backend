package site.dogether.dailytodocertification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DailyTodoCertificationReviewResult {

    APPROVE("인정"),
    REJECT("노인정")
    ;

    private final String description;
}
