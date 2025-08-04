package site.dogether.challengegroup.controller.v1.dto.response;

import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;

public record JoinChallengeGroupApiResponseV1(
    String groupName,
    int duration,
    int maximumMemberCount,
    String startAt,
    String endAt
) {
    public static JoinChallengeGroupApiResponseV1 from(JoinChallengeGroupDto joinChallengeGroupDto) {
        return new JoinChallengeGroupApiResponseV1(
            joinChallengeGroupDto.groupName(),
            joinChallengeGroupDto.duration(),
            joinChallengeGroupDto.maximumMemberCount(),
            joinChallengeGroupDto.startAt(),
            joinChallengeGroupDto.endAt()
        );
    }
}
