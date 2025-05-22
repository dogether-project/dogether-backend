package site.dogether.member.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.entity.LastSelectedChallengeGroupRecord;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodohistory.entity.DailyTodoHistoryRead;
import site.dogether.member.exception.InvalidMemberException;
import site.dogether.memberactivity.entity.DailyTodoStats;
import site.dogether.notification.entity.NotificationToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(name = "profile_image_url", length = 500, nullable = false)
    private String profileImageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ToString.Exclude
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<NotificationToken> notificationTokens;

    @ToString.Exclude
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<ChallengeGroupMember> challengeGroupMembers;

    @ToString.Exclude
    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.REMOVE)
    private List<DailyTodoCertification> dailyTodoCertifications;

    @ToString.Exclude
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<DailyTodo> dailyTodos;

    @ToString.Exclude
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<DailyTodoHistoryRead> dailyTodoHistoryRead;

    @ToString.Exclude
    @OneToOne(mappedBy = "member", cascade = CascadeType.REMOVE)
    private DailyTodoStats dailyTodoStats;

    @ToString.Exclude
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<LastSelectedChallengeGroupRecord> lastSelectedChallengeGroupRecords;

    public static Member create(final String providerId, final String name) {
        return new Member(null, providerId, name, saveRandomProfileImageUrl(), LocalDateTime.now());
    }

    private static String saveRandomProfileImageUrl() {
        List<String> profileImageUrls = List.of(
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/blue_1.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/blue_2.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/blue_3.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/blue_4.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/blue_5.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/red_1.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/red_2.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/red_3.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/red_4.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/red_5.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/yellow_1.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/yellow_2.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/yellow_3.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/yellow_4.png",
                "https://dogether-bucket-dev.s3.ap-northeast-2.amazonaws.com/member_profile_image/yellow_5.png"
        );

        return profileImageUrls.get((int) (Math.random() * profileImageUrls.size()));
    }

    public Member(
        final Long id,
        final String providerId,
        final String name,
        final String profileImageUrl,
        final LocalDateTime createdAt
    ) {
        validateProviderId(providerId);
        validateName(name);

        this.id = id;
        this.providerId = providerId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
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

    @Override
    public String toString() {
        return "Member{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", createdAt=" + createdAt +
            '}';
    }
}
