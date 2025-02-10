package site.dogether.challengegroup.domain;

import lombok.Getter;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;

@Getter
public class ChallengeGroup {

    private final Long id;
    private final String name;
    private final int maximumMemberCount;
    private final ChallengeGroupStartAtOption startAtOption;
    private final ChallengeGroupDurationOption durationOption;
    private final int maximumTodoCount;
    private final ChallengeGroupStatus status;

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
    }

    private String validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidChallengeGroupException("챌린지 그룹 이름은 필수 입력값입니다.");
        }
        if (name.isEmpty() || name.length() > 20) {
            throw new InvalidChallengeGroupException("챌린지 그룹 이름은 1자 이상 20자 이하로 입력해주세요.");
        }
        return name;
    }

    private int validateMaximumMemberCount(final int maximumMemberCount) {
        if (maximumMemberCount < 2 || maximumMemberCount > 20) {
            throw new InvalidChallengeGroupException("챌린지 그룹 최대 인원은 2명 이상 20명 이하로 입력해주세요.");
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
        if (maximumTodoCount < 2 || maximumTodoCount > 10) {
            throw new InvalidChallengeGroupException("챌린지 그룹 최대 할 일 개수는 2개 이상 10개 이하로 입력해주세요.");
        }
        return maximumTodoCount;
    }

    private ChallengeGroupStatus setStatus() {
        if (startAtOption == ChallengeGroupStartAtOption.TOMORROW) {
            return ChallengeGroupStatus.READY;
        }
        return ChallengeGroupStatus.RUNNING;
    }

}
