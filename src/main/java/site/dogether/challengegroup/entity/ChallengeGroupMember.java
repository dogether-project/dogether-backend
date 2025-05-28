package site.dogether.challengegroup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.member.entity.Member;

import java.time.LocalDateTime;
import java.util.Objects;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenge_group_member",
        uniqueConstraints = @UniqueConstraint(columnNames = {"challenge_group_id", "member_id"}))
@Entity
public class ChallengeGroupMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "challenge_group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChallengeGroup challengeGroup;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ChallengeGroupMember(final ChallengeGroup challengeGroup, final Member member) {
        this(null, challengeGroup, member, LocalDateTime.now());
    }

    public ChallengeGroupMember(
        final Long id,
        final ChallengeGroup challengeGroup,
        final Member member,
        final LocalDateTime createdAt
    ) {
        this.id = id;
        this.challengeGroup = challengeGroup;
        this.member = member;
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ChallengeGroupMember that = (ChallengeGroupMember) o;
        return Objects.equals(id, that.id) && Objects.equals(challengeGroup, that.challengeGroup) && Objects.equals(member, that.member);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, challengeGroup, member);
    }
}
