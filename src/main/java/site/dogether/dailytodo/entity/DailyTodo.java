package site.dogether.dailytodo.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodo.exception.InvalidDailyTodoException;
import site.dogether.dailytodo.exception.NotCertifyPendingDailyTodoException;
import site.dogether.dailytodo.exception.NotCreatedTodayDailyTodoException;
import site.dogether.dailytodo.exception.NotDailyTodoWriterException;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodohistory.entity.DailyTodoHistory;
import site.dogether.member.entity.Member;
import site.dogether.memberactivity.entity.DailyTodoStats;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static site.dogether.dailytodo.entity.DailyTodoStatus.CERTIFY_COMPLETED;
import static site.dogether.dailytodo.entity.DailyTodoStatus.CERTIFY_PENDING;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo")
@Entity
public class DailyTodo extends BaseEntity {

    public static final int MAXIMUM_ALLOWED_CONTENT_LENGTH = 400;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "challenge_group_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ChallengeGroup challengeGroup;

    @JoinColumn(name = "writer_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "content", length = 400, nullable = false, updatable = false)
    private String content;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private DailyTodoStatus status;

    @Column(name = "written_at", nullable = false, updatable = false)
    private LocalDateTime writtenAt;

    @ToString.Exclude
    @OneToOne(mappedBy = "dailyTodo", cascade = CascadeType.REMOVE)
    private DailyTodoHistory dailyTodoHistory;

    @ToString.Exclude
    @OneToOne(mappedBy = "dailyTodo", cascade = CascadeType.REMOVE)
    private DailyTodoCertification dailyTodoCertification;

    public DailyTodo(
        final ChallengeGroup challengeGroup,
        final Member member,
        final String content
    ) {
        this(
            null,
            challengeGroup,
            member,
            content,
            CERTIFY_PENDING,
            LocalDateTime.now()
        );
    }

    public DailyTodo(
        final Long id,
        final ChallengeGroup challengeGroup,
        final Member member,
        final String content,
        final DailyTodoStatus status,
        final LocalDateTime writtenAt
    ) {
        validateChallengeGroup(challengeGroup);
        validateMember(member);
        validateContent(content);
        validateStatus(status);
        validateWrittenAt(writtenAt);

        this.id = id;
        this.challengeGroup = challengeGroup;
        this.member = member;
        this.content = content;
        this.status = status;
        this.writtenAt = writtenAt;
    }

    private void validateChallengeGroup(final ChallengeGroup challengeGroup) {
        if (challengeGroup == null) {
            throw new InvalidDailyTodoException("데일리 투두 챌린지 그룹으로 null을 입력할 수 없습니다.");
        }
    }

    private void validateMember(final Member member) {
        if (member == null) {
            throw new InvalidDailyTodoException("데일리 투두 작성자로 null을 입력할 수 없습니다.");
        }
    }

    private void validateContent(final String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidDailyTodoException(String.format("데일리 투두 내용으로 null 혹은 공백을 입력할 수 없습니다. (%s)", content));
        }

        if (content.length() > MAXIMUM_ALLOWED_CONTENT_LENGTH) {
            throw new InvalidDailyTodoException(String.format("데일리 투두 내용은 %d자 이하만 입력할 수 있습니다. (%d) (%s)", MAXIMUM_ALLOWED_CONTENT_LENGTH, content.length(), content));
        }
    }

    private void validateStatus(final DailyTodoStatus status) {
        if (status == null) {
            throw new InvalidDailyTodoException("데일리 투두 상태로 null을 입력할 수 없습니다.");
        }
    }

    private void validateWrittenAt(final LocalDateTime writtenAt) {
        if (writtenAt == null) {
            throw new InvalidDailyTodoException("데일리 투두 작성일로 null을 입력할 수 없습니다.");
        }
    }

    public boolean isCertifyPending() {
        return status == CERTIFY_PENDING;
    }

    public DailyTodoCertification certify(
        final Member writer,
        final String certifyContent,
        final String certifyMediaUrl,
        final DailyTodoStats dailyTodoStats
    ) {
        validateWriter(writer);
        validateStatusIsCertifyPending();
        validateWrittenToday();

        final DailyTodoCertification dailyTodoCertification = new DailyTodoCertification(this, certifyContent, certifyMediaUrl);
        dailyTodoStats.increaseCertificatedCount();
        status = CERTIFY_COMPLETED;

        return dailyTodoCertification;
    }

    private void validateWriter(final Member target) {
        if (!isWriter(target)) {
            throw new NotDailyTodoWriterException(String.format("데일리 투두 작성자 외에는 투두 인증을 생성할 수 없습니다. (%s) (%s)", this, target));
        }
    }

    private void validateStatusIsCertifyPending() {
        if (status != CERTIFY_PENDING) {
            throw new NotCertifyPendingDailyTodoException(String.format("인증 대기 상태가 아닌 데일리 투두는 인증을 생성할 수 없습니다. (%s)", this));
        }
    }

    private void validateWrittenToday() {
        final boolean writtenToday = writtenAt.toLocalDate().isEqual(LocalDate.now());
        if (!writtenToday) {
            throw new NotCreatedTodayDailyTodoException(String.format("데일리 투두가 작성된 당일에만 투두 인증을 생성할 수 있습니다. (%s)", this));
        }
    }

    public boolean isWriter(final Member target) {
        return member.equals(target);
    }

    public boolean isCertifyCompleted() {
        return status == CERTIFY_COMPLETED;
    }

    public Long getWriterId() {
        return member.getId();
    }

    public String getMemberName() {
        return member.getName();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        final DailyTodo dailyTodo = (DailyTodo) object;
        return Objects.equals(id, dailyTodo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
