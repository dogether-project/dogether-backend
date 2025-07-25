package site.dogether.challengegroup.service.dto;

import java.time.format.DateTimeFormatter;
import site.dogether.challengegroup.entity.ChallengeGroup;

public record JoiningChallengeGroupDto(
    Long groupId,
    String groupName,
    int currentMemberCount,
    int maximumMemberCount,
    String joinCode,
    String status,
    String startAt,
    String endAt,
    int progressDay,
    double progressRate
) {
    public static JoiningChallengeGroupDto from(
            ChallengeGroup joiningGroup,
            int currentMemberCount
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd");

        return new JoiningChallengeGroupDto(
            joiningGroup.getId(),
            joiningGroup.getName(),
            currentMemberCount,
            joiningGroup.getMaximumMemberCount(),
            joiningGroup.getJoinCode(),
            joiningGroup.getStatus().name(),
            joiningGroup.getStartAt().format(formatter),
            joiningGroup.getEndAt().format(formatter),
            joiningGroup.getProgressDay(),
            joiningGroup.getProgressRate()
        );
    }
}
