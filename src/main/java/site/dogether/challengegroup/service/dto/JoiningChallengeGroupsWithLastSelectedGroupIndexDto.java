package site.dogether.challengegroup.service.dto;

import java.util.List;

public record JoiningChallengeGroupsWithLastSelectedGroupIndexDto(
    int lastSelectedGroupIndex,
    List<JoiningChallengeGroupDto> joiningChallengeGroups
) {}
