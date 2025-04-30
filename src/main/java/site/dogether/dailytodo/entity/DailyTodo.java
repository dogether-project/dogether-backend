package site.dogether.dailytodo.entity;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodo.exception.InvalidDailyTodoException;
import site.dogether.dailytodo.exception.NotDailyTodoWriterException;
import site.dogether.member.entity.Member;

import java.time.LocalDate;
import java.util.Optional;

import static site.dogether.dailytodo.entity.DailyTodoStatus.*;

@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo")
@Entity
public class DailyTodo extends BaseEntity {

    public static final int MAXIMUM_ALLOWED_CONTENT_LENGTH = 20;
    public static final int MAXIMUM_ALLOWED_REJECT_REASON_LENGTH = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "challenge_group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChallengeGroup challengeGroup;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "content", length = 30, nullable = false)
    private String content;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private DailyTodoStatus status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

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
            null
        );
    }

    public DailyTodo(
        final Long id,
        final ChallengeGroup challengeGroup,
        final Member member,
        final String content,
        final DailyTodoStatus status,
        final String rejectReason
    ) {
        validateChallengeGroup(challengeGroup);
        validateMember(member);
        validateContent(content);
        validateStatus(status);
        validateRejectReason(status, rejectReason);

        this.id = id;
        this.challengeGroup = challengeGroup;
        this.member = member;
        this.content = content;
        this.status = status;
        this.rejectReason = rejectReason;
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
            throw new InvalidDailyTodoException(String.format("데일리 투두 내용은 %d자 이하만 입력할 수 있습니다. (%d)", MAXIMUM_ALLOWED_CONTENT_LENGTH, content.length()));
        }
    }

    private void validateStatus(final DailyTodoStatus status) {
        if (status == null) {
            throw new InvalidDailyTodoException("데일리 투두 상태로 null을 입력할 수 없습니다.");
        }
    }

    private void validateRejectReason(final DailyTodoStatus status, final String rejectReason) {
        if (status != REJECT && rejectReason != null) {
            throw new InvalidDailyTodoException(String.format("데일리 투두가 노인정 상태가 아니면 노인정 사유를 입력할 수 없습니다. (%s)", rejectReason));
        }

        if (status == REJECT && (rejectReason == null || rejectReason.isBlank())) {
            throw new InvalidDailyTodoException(String.format("노인정 사유로 null 혹은 공백을 입력할 수 없습니다. (%s)", rejectReason));
        }

        if (status == REJECT && rejectReason.length() > MAXIMUM_ALLOWED_REJECT_REASON_LENGTH) {
            throw new InvalidDailyTodoException(String.format("노인정 사유는 %d자 이하만 입력할 수 있습니다. (%d)", MAXIMUM_ALLOWED_REJECT_REASON_LENGTH, rejectReason.length()));
        }
    }

    public void review(final DailyTodoStatus result, final String rejectReason) {
        validateRejectReason(result, rejectReason);

        this.status = result;
        this.rejectReason = rejectReason;
    }

    public void validateWriter(final Member target) {
        if (!member.equals(target)) {
            throw new NotDailyTodoWriterException(String.format("데일리 투두 작성자가 아닙니다. (%s) (%s)", this, target));
        }
    }

    public void changeStatusReviewPending() {
        status = REVIEW_PENDING;
    }

    public boolean isCertifyPending() {
        return status == CERTIFY_PENDING;
    }

    public boolean isApproved() {
        return status == APPROVE;
    }

    public boolean isRejected() {
        return status == REJECT;
    }

    public boolean createdToday() {
        return createdAt.toLocalDate()
            .isEqual(LocalDate.now());
    }

    public Long getId() {
        return id;
    }

    public ChallengeGroup getChallengeGroup() {
        return challengeGroup;
    }

    public String getContent() {
        return content;
    }

    public DailyTodoStatus getStatus() {
        return status;
    }

    public Optional<String> getRejectReason() {
        return Optional.ofNullable(rejectReason);
    }

    public String getStatusDescription() {
        return status.getDescription();
    }

    public Member getMember() {
        return member;
    }

    public Long getMemberId() {
        return member.getId();
    }

    public String getMemberName() {
        return member.getName();
    }
}
