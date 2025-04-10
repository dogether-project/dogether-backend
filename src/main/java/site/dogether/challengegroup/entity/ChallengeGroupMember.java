package site.dogether.challengegroup.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.common.audit.entity.BaseTimeEntity;
import site.dogether.member.entity.Member;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenge_group_member")
@Entity
public class ChallengeGroupMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "challenge_group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChallengeGroup challengeGroup;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    public ChallengeGroupMember(final ChallengeGroup challengeGroup, final Member member) {
        this(null, challengeGroup, member);
    }

    public ChallengeGroupMember(
        final Long id,
        final ChallengeGroup challengeGroup,
        final Member member
    ) {
        this.id = id;
        this.challengeGroup = challengeGroup;
        this.member = member;
    }
}
