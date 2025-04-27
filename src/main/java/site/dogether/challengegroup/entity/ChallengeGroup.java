package site.dogether.challengegroup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
    public static final int MAX_MAXIMUM_MEMBER_COUNT = 20;

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
        return new ChallengeGroup(
            null,
            validateGroupNameLength(name),
            validateMaximumMemberCount(maximumMemberCount),
            startAt,
            endAt,
            generateJoinCode(),
            setStatus(startAt)
        );
    }

    private static int validateMaximumMemberCount(int maximumMemberCount) {
        if (maximumMemberCount < MIN_MAXIMUM_MEMBER_COUNT || maximumMemberCount > MAX_MAXIMUM_MEMBER_COUNT) {
            throw new InvalidChallengeGroupException(String.format(
                    "챌린지 그룹 최대 인원은 %d명 이상, %d명 이하만 가능합니다. (input : %d)",
                    MIN_MAXIMUM_MEMBER_COUNT, MAX_MAXIMUM_MEMBER_COUNT, maximumMemberCount));
        }
        return maximumMemberCount;
    }

    private static String validateGroupNameLength(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidChallengeGroupException(String.format("챌린지 그룹 이름으로 null 혹은 공백을 입력할 수 없습니다. (input : %s)", name));
        }

        if (name.length() > MAXIMUM_GROUP_NAME_LENGTH) {
            throw new InvalidChallengeGroupException(String.format(
                    "챌린지 그룹 이름은 1자 이상, %d자 이하만 가능합니다. (input : %s)", MAXIMUM_GROUP_NAME_LENGTH, name));
        }
        return name;
    }

    private static String generateJoinCode() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    private static ChallengeGroupStatus setStatus(final LocalDate startAt) {
        if (startAt.equals(LocalDate.now())) {
            return ChallengeGroupStatus.RUNNING;
        }
        return ChallengeGroupStatus.READY;
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

    // TODO : startAT이 endAt보다 늦은 날짜가 아닌지 검증

    public boolean isFinished() {
        return this.status == ChallengeGroupStatus.FINISHED;
    }

    public int getDurationDays() {
        return endAt.getDayOfYear() - startAt.getDayOfYear();
    }

    public boolean isRunning() {
        return status == ChallengeGroupStatus.RUNNING;
    }
}
