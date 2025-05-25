package site.dogether.dailytodo.service.dto;

import java.time.LocalDate;
import java.util.Optional;

public class FindMyDailyTodosConditionDto {

    private final Long memberId;
    private final Long groupId;
    private final LocalDate createdAt;
    private final String dailyTodoCertificationReviewStatus;

    public FindMyDailyTodosConditionDto(
        final Long memberId,
        final Long groupId,
        final LocalDate createdAt,
        final String dailyTodoCertificationReviewStatus
    ) {
        this.memberId = memberId;
        this.groupId = groupId;
        this.createdAt = createdAt;
        this.dailyTodoCertificationReviewStatus = dailyTodoCertificationReviewStatus;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public Optional<String> findDailyTodoCertificationReviewStatus() {
        return Optional.ofNullable(dailyTodoCertificationReviewStatus);
    }
}
