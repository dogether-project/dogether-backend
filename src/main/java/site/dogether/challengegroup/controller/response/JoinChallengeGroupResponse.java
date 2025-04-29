package site.dogether.challengegroup.controller.response;

import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;

public record JoinChallengeGroupResponse(
    String groupName,
    int maximumMemberCount,
    String startAt,
    String endAt
) {
    public static JoinChallengeGroupResponse from(JoinChallengeGroupDto joinChallengeGroupDto) {
        return new JoinChallengeGroupResponse(
            joinChallengeGroupDto.groupName(),
            joinChallengeGroupDto.maximumMemberCount(),
            joinChallengeGroupDto.startAt(),
            joinChallengeGroupDto.endAt()
        );
    }
}
