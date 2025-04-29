package site.dogether.docs.challengegroup.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.docs.util.RestDocsEnumType;

@Getter
@RequiredArgsConstructor
public enum ChallengeGroupStatusDocs implements RestDocsEnumType {

    READY("시작 대기", "READY"),
    RUNNING("진행중", "RUNNING"),
    D_DAY("종료일", "D_DAY"),
    FINISHED("종료일도 지남", "FINISHED"),;

    private static final int enumValueCount = ChallengeGroupStatus.values().length;

    private final String description;
    private final String requestValue;

    public static RestDocsEnumType[] getValues() {
        final ChallengeGroupStatusDocs[] values = ChallengeGroupStatusDocs.values();
        RestDocsEnumType.checkDocsValueCountIsEqualToEnumValueCount(enumValueCount, values.length);
        return values;
    }
}
