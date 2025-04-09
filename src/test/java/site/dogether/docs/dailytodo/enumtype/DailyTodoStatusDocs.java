package site.dogether.docs.dailytodo.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.docs.util.RestDocsEnumType;

@Getter
@RequiredArgsConstructor
public enum DailyTodoStatusDocs implements RestDocsEnumType {

    CERTIFY_PENDING("인증 대기", "CERTIFY_PENDING"),
    REVIEW_PENDING("검사 대기", "REVIEW_PENDING"),
    APPROVE("인정", "APPROVE"),
    REJECT("노인정", "REJECT"),
    ;

    private static final int enumValueCount = DailyTodoStatus.values().length;

    private final String description;
    private final String requestValue;

    public static RestDocsEnumType[] getValues() {
        final DailyTodoStatusDocs[] values = DailyTodoStatusDocs.values();
        RestDocsEnumType.checkDocsValueCountIsEqualToEnumValueCount(enumValueCount, values.length);
        return values;
    }
}
