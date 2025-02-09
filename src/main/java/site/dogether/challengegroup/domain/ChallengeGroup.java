package site.dogether.challengegroup.domain;

import site.dogether.dailytodo.domain.exception.InvalidDailyTodoException;

import java.time.LocalDateTime;

public class ChallengeGroup {

    private static final int MINIMUM_LIMIT_TODO_COUNT = 2;

    private final Long id;
    private final String name;
    private final int maximumMemberCount;
    private final int maximumLimitTodoCount;
    private final String joinCode;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final ChallengeGroupStatus status;

    public ChallengeGroup(
        final Long id,
        final String name,
        final int maximumMemberCount,
        final int maximumLimitTodoCount,
        final String joinCode,
        final LocalDateTime startAt,
        final LocalDateTime endAt,
        final ChallengeGroupStatus status
    ) {
        this.id = id;
        this.name = name;
        this.maximumMemberCount = maximumMemberCount;
        this.maximumLimitTodoCount = maximumLimitTodoCount;
        this.joinCode = joinCode;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
    }

    public void checkEnableTodoCount(final int todoCount) {
        if (todoCount < MINIMUM_LIMIT_TODO_COUNT || todoCount > maximumLimitTodoCount) {
            final String exceptionMessage = String.format("데일리 투두는 %d ~ %d개만 등록할 수 있습니다.", MINIMUM_LIMIT_TODO_COUNT, maximumLimitTodoCount);
            throw new InvalidDailyTodoException(exceptionMessage);
        }
    }

    public boolean isRunning() {
        return status == ChallengeGroupStatus.RUNNING;
    }
}
