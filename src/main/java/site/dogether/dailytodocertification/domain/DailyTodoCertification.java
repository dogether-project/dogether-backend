package site.dogether.dailytodocertification.domain;

import lombok.Getter;
import site.dogether.dailytodo.domain.DailyTodo;
import site.dogether.dailytodocertification.domain.exception.InvalidDailyTodoCertificationException;
import site.dogether.member.domain.Member;

import java.time.LocalDateTime;

@Getter
public class DailyTodoCertification {

    private static final int MINIMUM_LIMIT_CONTENT_LENGTH = 2;
    private static final int MAXIMUM_LIMIT_CONTENT_LENGTH = 50;

    private final Long id;
    private final DailyTodo dailyTodo;
    private final Member member;
    private final String content;
    private final LocalDateTime createdAt;

    public static DailyTodoCertification create(
        final String content,
        final DailyTodo dailyTodo,
        final Member member
    ) {
        return new DailyTodoCertification(
            null,
            dailyTodo,
            member,
            content,
            LocalDateTime.now()
        );
    }

    public DailyTodoCertification(
        final Long id,
        final DailyTodo dailyTodo,
        final Member member,
        final String content,
        final LocalDateTime createdAt
    ) {
        validateContent(content);

        this.id = id;
        this.dailyTodo = dailyTodo;
        this.member = member;
        this.content = content;
        this.createdAt = createdAt;
    }

    private void validateContent(final String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidDailyTodoCertificationException("데일리 투두 수행 인증 본문으로 공백을 입력할 수 없습니다.");
        }

        if (content.length() < MINIMUM_LIMIT_CONTENT_LENGTH || content.length() > MAXIMUM_LIMIT_CONTENT_LENGTH) {
            final String exceptionMessage = String.format("데일리 투두 수행 인증 본문은 %d ~ %d길이의 문자열만 가능합니다.",
                MINIMUM_LIMIT_CONTENT_LENGTH,
                MAXIMUM_LIMIT_CONTENT_LENGTH
            );
            throw new InvalidDailyTodoCertificationException(exceptionMessage);
        }
    }
}
