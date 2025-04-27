package site.dogether.challengegroup.service.dto;

import lombok.Getter;
import site.dogether.dailytodo.entity.MyTodoSummary;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ChallengeGroupMemberRankInfo {

    private final String memberName;
    private final MyTodoSummary myTodoSummary;
    private final LocalDateTime joinedAt;
    private final LocalDate groupStartAt;
    private final LocalDate groupEndAt;

    public ChallengeGroupMemberRankInfo(
            final String memberName,
            final MyTodoSummary myTodoSummary,
            final LocalDateTime joinedAt,
            final LocalDate groupStartAt,
            final LocalDate groupEndAt
    ) {
        this.memberName = memberName;
        this.myTodoSummary = myTodoSummary;
        this.joinedAt = joinedAt;
        this.groupStartAt = groupStartAt;
        this.groupEndAt = groupEndAt;
    }
}
