package site.dogether.dailytodo.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupJpaEntity;
import site.dogether.common.audit.entity.BaseTimeEntity;
import site.dogether.dailytodo.domain.DailyTodoStatus;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo")
@Entity
public class DailyTodoJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "challenge_group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChallengeGroupJpaEntity challengeGroup;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private MemberJpaEntity member;

    @Column(name = "content", length = 30, nullable = false)
    private String content;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private DailyTodoStatus status;

    public DailyTodoJpaEntity(
        final ChallengeGroupJpaEntity challengeGroup,
        final MemberJpaEntity member,
        final String content,
        final DailyTodoStatus status
    ) {
        this(null, challengeGroup, member, content, status);
    }

    public DailyTodoJpaEntity(
        final Long id,
        final ChallengeGroupJpaEntity challengeGroup,
        final MemberJpaEntity member,
        final String content,
        final DailyTodoStatus status
    ) {
        this.id = id;
        this.challengeGroup = challengeGroup;
        this.member = member;
        this.content = content;
        this.status = status;
    }
}
