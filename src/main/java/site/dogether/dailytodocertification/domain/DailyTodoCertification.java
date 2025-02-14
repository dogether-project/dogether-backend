package site.dogether.dailytodocertification.domain;

import lombok.Getter;
import site.dogether.challengegroup.domain.ChallengeGroup;
import site.dogether.dailytodo.domain.DailyTodo;
import site.dogether.dailytodo.domain.DailyTodoStatus;
import site.dogether.dailytodocertification.domain.exception.InvalidDailyTodoCertificationException;
import site.dogether.member.domain.Member;

import java.time.LocalDateTime;

@Getter
public class DailyTodoCertification {

    private static final int MINIMUM_LIMIT_CONTENT_LENGTH = 2;
    private static final int MAXIMUM_LIMIT_CONTENT_LENGTH = 50;

    private final Long id;
    private final DailyTodo dailyTodo;
    private final Member reviewer;
    private final String content;
    private final LocalDateTime createdAt;

    public static DailyTodoCertification create(
        final String content,
        final DailyTodo dailyTodo,
        final Member reviewer
    ) {
        return new DailyTodoCertification(
            null,
            dailyTodo,
            reviewer,
            content,
            LocalDateTime.now()
        );
    }

    public DailyTodoCertification(
        final Long id,
        final DailyTodo dailyTodo,
        final Member reviewer,
        final String content,
        final LocalDateTime createdAt
    ) {
        validateContent(content);

        this.id = id;
        this.dailyTodo = dailyTodo;
        this.reviewer = reviewer;
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

    public boolean checkReviewer(final Long reviewerId) {
        return reviewer.getId().equals(reviewerId);
    }

    public String getDailyTodoContent() {
        return dailyTodo.getContent();
    }

    public ChallengeGroup getChallengeGroup() {
        return dailyTodo.getChallengeGroup();
    }

    public String getReviewerName() {
        return reviewer.getName();
    }

    public String getDoerName() {
        return dailyTodo.getMember().getName();
    }

    public DailyTodo review(final String result, final String rejectReason) {
        return new DailyTodo(
            dailyTodo.getId(),
            dailyTodo.getContent(),
            DailyTodoStatus.valueOf(result),
            rejectReason,
            dailyTodo.getCreatedAt(),
            dailyTodo.getMember(),
            dailyTodo.getChallengeGroup()
        );
    }
}
