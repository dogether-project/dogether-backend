package site.dogether.dailytodo.domain;

import lombok.Getter;
import site.dogether.challengegroup.domain.ChallengeGroup;
import site.dogether.dailytodo.domain.exception.InvalidDailyTodoException;
import site.dogether.dailytodocertification.domain.exception.InvalidDailyTodoRejectReasonException;
import site.dogether.member.domain.Member;

import java.time.LocalDateTime;
import java.util.Optional;

import static site.dogether.dailytodo.domain.DailyTodoStatus.*;

@Getter
public class DailyTodo {

    private static final int MINIMUM_LIMIT_CONTENT_LENGTH = 1;
    private static final int MAXIMUM_LIMIT_CONTENT_LENGTH = 30;
    private static final int MINIMUM_LIMIT_REJECT_REASON_LENGTH = 1;
    private static final int MAXIMUM_LIMIT_REJECT_REASON_LENGTH = 100;

    private final Long id;
    private final String content;
    private final DailyTodoStatus status;
    private final String rejectReason;
    private final LocalDateTime createdAt;
    private final Member member;
    private final ChallengeGroup challengeGroup;

    public static DailyTodo create(
        final String content,
        final Member member,
        final ChallengeGroup challengeGroup
    ) {
        return new DailyTodo(
            null,
            content,
            CERTIFY_PENDING,
            null,
            LocalDateTime.now(),
            member,
            challengeGroup
        );
    }

    public DailyTodo(
        final Long id,
        final String content,
        final DailyTodoStatus status,
        final String rejectReason,
        final LocalDateTime createdAt,
        final Member member,
        final ChallengeGroup challengeGroup
    ) {
        validateContent(content);
        validateRejectReason(status, rejectReason);

        this.id = id;
        this.content = content;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.member = member;
        this.challengeGroup = challengeGroup;
    }

    private void validateContent(final String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidDailyTodoException("데일리 투두 내용으로 공백을 입력할 수 없습니다.");
        }

        if (content.length() < MINIMUM_LIMIT_CONTENT_LENGTH || content.length() > MAXIMUM_LIMIT_CONTENT_LENGTH) {
            throw new InvalidDailyTodoException("데일리 투두 내용은 %d ~ %d 길이의 문자열만 입력할 수 있습니다. - %s");
        }
    }

    private void validateRejectReason(final DailyTodoStatus status, final String rejectReason) {
        if (status != REJECT && rejectReason != null) {
            throw new InvalidDailyTodoRejectReasonException("데일리 투두가 노인정 상태일 때만 노인정 사유를 입력할 수 있습니다. - " + status.name());
        }

        if (status != REJECT) {
            return;
        }

        if (rejectReason == null || rejectReason.isBlank()) {
            throw new InvalidDailyTodoRejectReasonException("데일리 투두가 노인정 상태라면 노인정 사유에 공백을 입력할 수 없습니다.");
        }

        if (rejectReason.length() < MINIMUM_LIMIT_REJECT_REASON_LENGTH || rejectReason.length() > MAXIMUM_LIMIT_REJECT_REASON_LENGTH) {
            final String exceptionMessage = String.format("데일리 투두 노인정 사유는 %d ~ %d길이의 문자열만 입력할 수 있습니다. - %s",
                MINIMUM_LIMIT_REJECT_REASON_LENGTH,
                MAXIMUM_LIMIT_REJECT_REASON_LENGTH,
                rejectReason
            );
            throw new InvalidDailyTodoRejectReasonException(exceptionMessage);
        }
    }

    public boolean checkOwner(final Long memberId) {
        return member.getId().equals(memberId);
    }

    public boolean isCertifyPendingStatus() {
        return status == CERTIFY_PENDING;
    }

    public boolean createdToday() {
        return createdAt.toLocalDate()
            .isEqual(LocalDateTime.now().toLocalDate());
    }

    public Long getMemberId() {
        return member.getId();
    }

    public String getStatusDescription() {
        return status.getDescription();
    }

    public Optional<String> getRejectReason() {
        return Optional.ofNullable(rejectReason);
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
}
