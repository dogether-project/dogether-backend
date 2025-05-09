package site.dogether.challengegroup.service.dto;

import java.time.format.DateTimeFormatter;
import site.dogether.challengegroup.entity.ChallengeGroup;

public record JoinChallengeGroupDto(
        String groupName,
        int duration,
        int maximumMemberCount,
        String startAt,
        String endAt
) {
    public static JoinChallengeGroupDto from(ChallengeGroup challengeGroup) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd");
        final String startAtFormatted = challengeGroup.getStartAt().format(formatter);
        final String endAtFormatted = challengeGroup.getEndAt().format(formatter);

        return new JoinChallengeGroupDto(
                challengeGroup.getName(),
                challengeGroup.getDuration(),
                challengeGroup.getMaximumMemberCount(),
                startAtFormatted,
                endAtFormatted
        );
    }
}
