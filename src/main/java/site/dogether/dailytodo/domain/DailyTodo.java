package site.dogether.dailytodo.domain;

import lombok.Getter;
import site.dogether.challengegroup.domain.ChallengeGroup;
import site.dogether.dailytodo.domain.exception.InvalidDailyTodoException;
import site.dogether.member.domain.Member;

import java.time.LocalDateTime;

@Getter
public class DailyTodo {

    private static final int MINIMUM_LIMIT_CONTENT_LENGTH = 2;
    private static final int MAXIMUM_LIMIT_CONTENT_LENGTH = 20;

    private final Long id;
    private final String content;
    private final DailyTodoStatus status;
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
            DailyTodoStatus.CERTIFY_PENDING,
            LocalDateTime.now(),
            member,
            challengeGroup
        );
    }

    public DailyTodo(
        final Long id,
        final String content,
        final DailyTodoStatus status,
        final LocalDateTime createdAt,
        final Member member,
        final ChallengeGroup challengeGroup
    ) {
        validateContent(content);

        this.id = id;
        this.content = content;
        this.status = status;
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

    public boolean checkOwner(final Long memberId) {
        return member.getId().equals(memberId);
    }

    public boolean isCertifyPendingStatus() {
        return status == DailyTodoStatus.CERTIFY_PENDING;
    }

    public boolean createdToday() {
        return createdAt.toLocalDate()
            .isEqual(LocalDateTime.now().toLocalDate());
    }
}
