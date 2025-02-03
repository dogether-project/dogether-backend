package site.dogether.challengegroup.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.dogether.challengegroup.domain.ChallengeGroupStatus;
import site.dogether.common.audit.entity.BaseTimeEntity;

import java.time.LocalDateTime;

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
}
