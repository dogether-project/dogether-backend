package site.dogether.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.member.exception.InvalidMemberException;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
@Entity
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", length = 100, nullable = false, unique = true)
    private String providerId;

    @Column(name = "name", length = 20, nullable = false, unique = true)
    private String name;

    @Column(name = "profile_image_url", length = 500, nullable = false)
    private String profileImageUrl;

    public static Member create(final String providerId, final String name, final String profileImageUrl) {
        return new Member(null, providerId, name, profileImageUrl);
    }

    public Member(final Long id, final String providerId, final String name, final String profileImageUrl) {
        validateProviderId(providerId);
        validateName(name);

        this.id = id;
        this.providerId = providerId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    private void validateProviderId(final String providerId) {
        if (providerId == null || providerId.isBlank()) {
            throw new InvalidMemberException(String.format("provider id는 null 혹은 공백을 입력할 수 없습니다. (input : %s)", name));
        }
    }

    private void validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidMemberException(String.format("회원 이름은 null 혹은 공백을 입력할 수 없습니다. (input : %s)", name));
        }
    }
}
