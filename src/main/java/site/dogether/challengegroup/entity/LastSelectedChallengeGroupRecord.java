package site.dogether.challengegroup.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.member.entity.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "last_selected_challenge_group_record")
@Entity
public class LastSelectedChallengeGroupRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "challenge_group_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ChallengeGroup challengeGroup;

    public LastSelectedChallengeGroupRecord(final Member member, final ChallengeGroup challengeGroup) {
        this(null, member, challengeGroup);
    }

    public LastSelectedChallengeGroupRecord(
        final Long id,
        final Member member,
        final ChallengeGroup challengeGroup
    ) {
        this.id = id;
        this.member = member;
        this.challengeGroup = challengeGroup;
    }

    public void updateChallengeGroup(final ChallengeGroup challengeGroup) {
        this.challengeGroup = challengeGroup;
    }
}
