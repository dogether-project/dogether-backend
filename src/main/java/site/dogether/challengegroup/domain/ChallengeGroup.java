package site.dogether.challengegroup.domain;

import java.util.UUID;
import lombok.Getter;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;
import site.dogether.dailytodo.domain.exception.InvalidDailyTodoException;

@Getter
public class ChallengeGroup {

    private static final int MINIMUM_LIMIT_TODO_COUNT = 2;
    private static final int MAXIMUM_LIMIT_TODO_COUNT = 10;
    private static final int MAXIMUM_GROUP_NAME_LENGTH = 20;
    public static final int MIN_MAXIMUM_MEMBER_COUNT = 2;
    public static final int MAX_MAXIMUM_MEMBER_COUNT = 20;
    public static final int JOIN_CODE_PARSING_START_INDEX = 0;
    public static final int JOIN_CODE_PARSING_END_INDEX = 6;

    private final Long id;
    private final String name;
    private final int maximumMemberCount;
    private final ChallengeGroupStartAtOption startAtOption;
    private final ChallengeGroupDurationOption durationOption;
    private final int maximumTodoCount;
    private final ChallengeGroupStatus status;
    private final String joinCode;

    public ChallengeGroup(
            final String name,
            final int maximumMemberCount,
            final String startAt,
            final int durationOption,
            final int maximumTodoCount
    ) {
        this.id = null;
        this.name = validateName(name);
        this.maximumMemberCount = validateMaximumMemberCount(maximumMemberCount);
        this.startAtOption = setStartAtOption(startAt);
        this.durationOption = setDurationOption(durationOption);
        this.maximumTodoCount = validateMaximumTodoCount(maximumTodoCount);
        this.status = setStatus();
        this.joinCode = createJoinCode();
    }

    public ChallengeGroup(
            final Long id,
            final String name,
            final int maximumMemberCount,
            final ChallengeGroupStartAtOption startAtOption,
            final ChallengeGroupDurationOption durationOption,
            final int maximumTodoCount,
            final ChallengeGroupStatus status,
            final String joinCode
    ) {
        this.id = id;
        this.name = name;
        this.maximumMemberCount = maximumMemberCount;
        this.startAtOption = startAtOption;
        this.durationOption = durationOption;
        this.maximumTodoCount = maximumTodoCount;
        this.status = status;
        this.joinCode = joinCode;
    }

    public void checkEnableTodoCount(final int todoCount) {
        if (todoCount < MINIMUM_LIMIT_TODO_COUNT || todoCount > maximumTodoCount) {
            final String exceptionMessage = String.format("데일리 투두는 %d ~ %d개만 등록할 수 있습니다.", MINIMUM_LIMIT_TODO_COUNT, maximumTodoCount);
            throw new InvalidDailyTodoException(exceptionMessage);
        }
    }

    public boolean isRunning() {
        return status == ChallengeGroupStatus.RUNNING;
    }

    public boolean isFinished() {
        return this.status == ChallengeGroupStatus.FINISHED;
    }

    private String validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidChallengeGroupException("챌린지 그룹 이름은 필수 입력값입니다.");
        }
        if (name.isEmpty() || name.length() > MAXIMUM_GROUP_NAME_LENGTH) {
            throw new InvalidChallengeGroupException("챌린지 그룹 이름은 1자 이상 "+ MAXIMUM_GROUP_NAME_LENGTH +"자 이하로 입력해주세요.");
        }
        return name;
    }

    private int validateMaximumMemberCount(final int maximumMemberCount) {
        if (maximumMemberCount < MIN_MAXIMUM_MEMBER_COUNT || maximumMemberCount > MAX_MAXIMUM_MEMBER_COUNT) {
            throw new InvalidChallengeGroupException("챌린지 그룹 최대 인원은 "+ MIN_MAXIMUM_MEMBER_COUNT +"명 이상 "
                    + MAX_MAXIMUM_MEMBER_COUNT + "명 이하로 입력해주세요.");
        }
        return maximumMemberCount;
    }
    
    private ChallengeGroupStartAtOption setStartAtOption(final String startAt) {
        return ChallengeGroupStartAtOption.from(startAt);
    }

    private ChallengeGroupDurationOption setDurationOption(final int durationOption) {
        return ChallengeGroupDurationOption.from(durationOption);
    }

    private int validateMaximumTodoCount(final int maximumTodoCount) {
        if (maximumTodoCount < MINIMUM_LIMIT_TODO_COUNT || maximumTodoCount > MAXIMUM_LIMIT_TODO_COUNT) {
            throw new InvalidChallengeGroupException("챌린지 그룹 최대 할 일 개수는 "+ MINIMUM_LIMIT_TODO_COUNT + "개 이상 "
                    + MAXIMUM_LIMIT_TODO_COUNT +"개 이하로 입력해주세요.");
        }
        return maximumTodoCount;
    }

    private ChallengeGroupStatus setStatus() {
        if (startAtOption == ChallengeGroupStartAtOption.TOMORROW) {
            return ChallengeGroupStatus.READY;
        }
        return ChallengeGroupStatus.RUNNING;
    }

    private String createJoinCode() {
        return UUID.randomUUID().toString()
                .substring(JOIN_CODE_PARSING_START_INDEX, JOIN_CODE_PARSING_END_INDEX);
    }
}
