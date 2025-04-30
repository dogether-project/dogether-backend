package site.dogether.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.member.exception.InvalidMemberException;

import java.util.List;
import java.util.Objects;

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

    public static Member create(final String providerId, final String name) {
        return new Member(null, providerId, name, saveRandomProfileImageUrl());
    }

    private static String saveRandomProfileImageUrl() {
        List<String> profileImageUrls = List.of(
                "s3://dogether-bucket-dev/member_profile_image/blue_1",
                "s3://dogether-bucket-dev/member_profile_image/blue_2",
                "s3://dogether-bucket-dev/member_profile_image/blue_3",
                "s3://dogether-bucket-dev/member_profile_image/blue_4",
                "s3://dogether-bucket-dev/member_profile_image/blue_5",
                "s3://dogether-bucket-dev/member_profile_image/red_1",
                "s3://dogether-bucket-dev/member_profile_image/red_2",
                "s3://dogether-bucket-dev/member_profile_image/red_3",
                "s3://dogether-bucket-dev/member_profile_image/red_4",
                "s3://dogether-bucket-dev/member_profile_image/red_5",
                "s3://dogether-bucket-dev/member_profile_image/yellow_1",
                "s3://dogether-bucket-dev/member_profile_image/yellow_2",
                "s3://dogether-bucket-dev/member_profile_image/yellow_3",
                "s3://dogether-bucket-dev/member_profile_image/yellow_4",
                "s3://dogether-bucket-dev/member_profile_image/yellow_5"
        );

        return profileImageUrls.get((int) (Math.random() * profileImageUrls.size()));
    }

    public Member(
        final Long id,
        final String providerId,
        final String name,
        final String profileImageUrl
    ) {
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

    @Override
    public boolean equals(final Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        final Member member = (Member) object;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
