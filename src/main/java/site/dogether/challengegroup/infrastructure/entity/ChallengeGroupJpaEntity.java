package site.dogether.challengegroup.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.dogether.challengegroup.domain.ChallengeGroup;
import site.dogether.challengegroup.domain.ChallengeGroupDurationOption;
import site.dogether.challengegroup.domain.ChallengeGroupStartAtOption;
import site.dogether.challengegroup.domain.ChallengeGroupStatus;
import site.dogether.common.audit.entity.BaseTimeEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenge_group")
@Entity
public class ChallengeGroupJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "maximum_member_count", nullable = false)
    private int maximumMemberCount;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "maximum_todo_count", nullable = false)
    private int maximumTodoCount;

    @Column(name = "join_code", length = 20, nullable = false, unique = true)
    private String joinCode;

    @Column(name = "status", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private ChallengeGroupStatus status;

    public ChallengeGroupJpaEntity(
        final String name,
        final int maximumMemberCount,
        final LocalDateTime startAt,
        final LocalDateTime endAt,
        final int maximumTodoCount,
        final String joinCode,
        final ChallengeGroupStatus status
    ) {
        this(
            null,
            name,
            maximumMemberCount,
            startAt,
            endAt,
            maximumTodoCount,
            joinCode,
            status
        );
    }

    public ChallengeGroupJpaEntity(
        final Long id,
        final String name,
        final int maximumMemberCount,
        final LocalDateTime startAt,
        final LocalDateTime endAt,
        final int maximumTodoCount,
        final String joinCode,
        final ChallengeGroupStatus status
    ) {
        this.id = id;
        this.name = name;
        this.maximumMemberCount = maximumMemberCount;
        this.startAt = startAt;
        this.endAt = endAt;
        this.maximumTodoCount = maximumTodoCount;
        this.joinCode = joinCode;
        this.status = status;
    }

    public static ChallengeGroupJpaEntity from(final ChallengeGroup challengeGroup) {
        final LocalDateTime now = LocalDateTime.now();

        return new ChallengeGroupJpaEntity(
            challengeGroup.getName(),
            challengeGroup.getMaximumMemberCount(),
            setStartAt(now, challengeGroup.getStartAtOption()),
            setEndAt(now, challengeGroup.getStartAtOption(), challengeGroup.getDurationOption()),
            challengeGroup.getMaximumTodoCount(),
            challengeGroup.getJoinCode(),
            challengeGroup.getStatus()
        );
    }

    private static LocalDateTime setStartAt(
            final LocalDateTime now,
            final ChallengeGroupStartAtOption startAtOption
    ) {
        if (startAtOption == ChallengeGroupStartAtOption.TODAY) {
            return now;
        }
        return now.plusDays(1);
    }

    private static LocalDateTime setEndAt(
            final LocalDateTime now,
            final ChallengeGroupStartAtOption startAtOption,
            final ChallengeGroupDurationOption durationOption
    ) {
        if (startAtOption == ChallengeGroupStartAtOption.TODAY) {
            return now.plusDays(durationOption.getValue());
        }
        return now.plusDays(durationOption.getValue() + 1);
    }

    public ChallengeGroup toDomain() {
        final ChallengeGroupStartAtOption startAtOption = ChallengeGroupStartAtOption.from(startAt, this.getCreatedAt());
        final ChallengeGroupDurationOption durationOption = ChallengeGroupDurationOption.from(startAt, endAt);

        return new ChallengeGroup(
            id,
            name,
            maximumMemberCount,
            startAtOption,
            durationOption,
            maximumTodoCount,
            status,
            joinCode
        );
    }
}
