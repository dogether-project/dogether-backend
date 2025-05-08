package site.dogether.challengegroup.entity;

import static site.dogether.challengegroup.entity.ChallengeGroupStatus.D_DAY;
import static site.dogether.challengegroup.entity.ChallengeGroupStatus.FINISHED;
import static site.dogether.challengegroup.entity.ChallengeGroupStatus.READY;
import static site.dogether.challengegroup.entity.ChallengeGroupStatus.RUNNING;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;
import site.dogether.common.audit.entity.BaseEntity;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenge_group")
@Entity
public class ChallengeGroup extends BaseEntity {

    private static final int MAXIMUM_GROUP_NAME_LENGTH = 10;
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

    @Column(name = "join_code", length = 20, nullable = false, unique = true)
    private String joinCode;

    @Column(name = "status", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private ChallengeGroupStatus status;

    public static ChallengeGroup create(
        final String name,
        final int maximumMemberCount,
        final LocalDate startAt,
        final LocalDate endAt
    ) {
        validateEndAtIsAfterStartAt(startAt, endAt);
        return new ChallengeGroup(
            null,
            validateGroupName(name),
            validateMaximumMemberCount(maximumMemberCount),
            startAt,
            endAt,
            generateJoinCode(),
            initStatus(startAt)
        );
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

    private static String generateJoinCode() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    private static ChallengeGroupStatus initStatus(final LocalDate startAt) {
        if (startAt.equals(LocalDate.now())) {
            return RUNNING;
        }
        return READY;
    }

    public ChallengeGroup(
        final Long id,
        final String name,
        final int maximumMemberCount,
        final LocalDate startAt,
        final LocalDate endAt,
        final String joinCode,
        final ChallengeGroupStatus status
    ) {
        this.id = id;
        this.name = name;
        this.maximumMemberCount = validateMaximumMemberCount(maximumMemberCount);
        this.startAt = startAt;
        this.endAt = endAt;
        this.joinCode = joinCode;
        this.status = status;
    }

    private boolean isReady() {
        return status == READY;
    }

    public boolean isRunning() {
        return status == RUNNING || status == D_DAY;
    }

    public boolean isFinished() {
        return status == FINISHED;
    }

    public int getProgressDay() {
        LocalDate today = LocalDate.now();
        if (isReady()) {
            return 0;
        }
        if (isFinished()) {
            return (int) ChronoUnit.DAYS.between(startAt, endAt) + 1;
        }
        return (int) ChronoUnit.DAYS.between(startAt, today) + 1;
    }

    public double getProgressRate() {
        int progressDay = getProgressDay();
        int duration = getDurationWithDDay();
        if (progressDay > duration) {
            return 1;
        }
        double rawRate = (double) progressDay / duration;
        return Math.round(rawRate * 100) / 100.0;
    }

    private int getDurationWithDDay() {
        return (int) ChronoUnit.DAYS.between(startAt, endAt) + 1;
    }

    public void updateStatus() {
        LocalDate now = LocalDate.now();
        if (status == READY && isStart(now)) {
            status = RUNNING;
            return;
        }
        if (status == RUNNING && isEnd(now)) {
            status = D_DAY;
            return;
        }
        if (status == D_DAY && now.isAfter(endAt)) {
            status = FINISHED;
        }
    }

    private boolean isStart(LocalDate now) {
        return startAt.isEqual(now);
    }

    private boolean isEnd(LocalDate now) {
        return endAt.isEqual(now);
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
