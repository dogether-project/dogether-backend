package site.dogether.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.member.entity.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_token")
@Entity
public class NotificationToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "token_value", length = 500, nullable = false, unique = true)
    private String value;

    public NotificationToken(final Member member, final String value) {
        this(null, member, value);
    }

    public NotificationToken(
        final Long id,
        final Member member,
        final String value
    ) {
        this.id = id;
        this.member = member;
        this.value = value;
    }
}
