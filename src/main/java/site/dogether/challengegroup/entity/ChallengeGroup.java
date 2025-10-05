package site.dogether.challengegroup.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;
import site.dogether.common.audit.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static site.dogether.challengegroup.entity.ChallengeGroupStatus.*;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenge_group")
@Entity
public class ChallengeGroup extends BaseEntity {

    private static final int MAXIMUM_GROUP_NAME_LENGTH = 200;
    private static final int MIN_MAXIMUM_MEMBER_COUNT = 2;
    private static final int MAX_MAXIMUM_MEMBER_COUNT = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "maximum_member_count", nullable = false)
    private int maximumMemberCount;

    @Column(name = "start_at", nullable = false)
    private LocalDate startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDate endAt;

    @Embedded
    private JoinCode joinCode;

    @Column(name = "status", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private ChallengeGroupStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ChallengeGroup(
            final String name,
            final int maximumMemberCount,
            final LocalDate startAt,
            final LocalDate endAt,
            final JoinCode joinCode,
            final LocalDateTime createdAt
    ) {
        validateEndAtIsAfterStartAt(startAt, endAt);
        this.name = validateGroupName(name);
        this.maximumMemberCount = validateMaximumMemberCount(maximumMemberCount);
        this.startAt = startAt;
        this.endAt = endAt;
        this.joinCode = joinCode;
        this.status = initStatus(startAt);
        this.createdAt = createdAt;
    }

    private static String validateGroupName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidChallengeGroupException(
                    String.format("챌린지 그룹 이름으로 null 혹은 공백을 입력할 수 없습니다. (name : %s)", name));
        }

        if (name.length() > MAXIMUM_GROUP_NAME_LENGTH) {
            throw new InvalidChallengeGroupException(String.format(
                    "챌린지 그룹 이름은 1자 이상, %d자 이하만 가능합니다. (name : %s)", MAXIMUM_GROUP_NAME_LENGTH, name));
        }
        return name;
    }

    private static int validateMaximumMemberCount(int maximumMemberCount) {
        if (maximumMemberCount < MIN_MAXIMUM_MEMBER_COUNT || maximumMemberCount > MAX_MAXIMUM_MEMBER_COUNT) {
            throw new InvalidChallengeGroupException(String.format(
                    "챌린지 그룹 최대 인원은 %d명 이상, %d명 이하만 가능합니다. (input : %d)",
                    MIN_MAXIMUM_MEMBER_COUNT, MAX_MAXIMUM_MEMBER_COUNT, maximumMemberCount));
        }
        return maximumMemberCount;
    }

    private static void validateEndAtIsAfterStartAt(LocalDate startAt, LocalDate endAt) {
        if (startAt.isAfter(endAt)) {
            throw new InvalidChallengeGroupException(
                    String.format("시작일은 종료일보다 늦을 수 없습니다. (startAt : %s, endAt : %s)", startAt, endAt)
            );
        }
    }

    private static ChallengeGroupStatus initStatus(final LocalDate startAt) {
        if (startAt.equals(LocalDate.now())) {
            return RUNNING;
        }
        return READY;
    }

    public boolean isRunning() {
        return status == RUNNING || status == D_DAY;
    }

    public boolean isFinished() {
        return status == FINISHED;
    }

    public int getProgressDay() {
        LocalDate today = LocalDate.now();
        if (today.isBefore(startAt)) {
            return 0;
        }
        if (today.isAfter(endAt)) {
            return (int) ChronoUnit.DAYS.between(startAt, endAt) + 1;
        }
        return (int) ChronoUnit.DAYS.between(startAt, today) + 1;
    }

    public double getProgressRate() {
        int progressDay = getProgressDay();
        int duration = getDuration();
        if (progressDay > duration) {
            return 1;
        }
        double rawRate = (double) progressDay / duration;
        return Math.round(rawRate * 100) / 100.0;
    }

    public int getDuration() {
        return (int) ChronoUnit.DAYS.between(startAt, endAt);
    }

    public void updateStatus() {
        final LocalDate now = LocalDate.now();
        if (now.isBefore(startAt)) {
            status = READY;
        } else if (now.isBefore(endAt)) {
            status = RUNNING;
        } else if (now.equals(endAt)) {
            status = D_DAY;
        } else {
            status = FINISHED;
        }
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        final ChallengeGroup that = (ChallengeGroup) object;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
