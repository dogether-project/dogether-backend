package site.dogether.member.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.common.audit.entity.BaseTimeEntity;
import site.dogether.member.domain.Member;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
@Entity
public class MemberJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", length = 100, nullable = false, unique = true)
    private String providerId;

    @Column(name = "name", length = 20, nullable = false, unique = true)
    private String name;

    public MemberJpaEntity(final String providerId, final String name) {
        this(null, providerId, name);
    }

    public MemberJpaEntity(final Long id, final String providerId, final String name) {
        this.id = id;
        this.providerId = providerId;
        this.name = name;
    }

    public MemberJpaEntity(final Member member) {
        this(member.getProviderId(), member.getName());
    }

    public Member toDomain() {
        return new Member(
                id,
                providerId,
                name
        );
    }
}
