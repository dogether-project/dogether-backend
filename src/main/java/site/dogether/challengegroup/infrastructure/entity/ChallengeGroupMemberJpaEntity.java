package site.dogether.challengegroup.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.dogether.challengegroup.domain.ChallengeGroup;
import site.dogether.common.audit.entity.BaseTimeEntity;
import site.dogether.member.domain.Member;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenge_group_member")
@Entity
public class ChallengeGroupMemberJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "challenge_group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChallengeGroupJpaEntity challengeGroup;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private MemberJpaEntity member;

    public ChallengeGroupMemberJpaEntity(final ChallengeGroupJpaEntity challengeGroup, final MemberJpaEntity member) {
        this(null, challengeGroup, member);
    }

    public ChallengeGroupMemberJpaEntity(
        final Long id,
        final ChallengeGroupJpaEntity challengeGroup,
        final MemberJpaEntity member
    ) {
        this.id = id;
        this.challengeGroup = challengeGroup;
        this.member = member;
    }

    public ChallengeGroup toChallengeGroupDomain() {
        return challengeGroup.toDomain();
    }

    public Member toMemberDomain() {
        return member.toDomain();
    }
}
