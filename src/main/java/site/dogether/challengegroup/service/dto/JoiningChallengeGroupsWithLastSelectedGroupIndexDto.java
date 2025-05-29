package site.dogether.challengegroup.service.dto;

import java.util.List;

public record JoiningChallengeGroupsWithLastSelectedGroupIndexDto(
    Integer lastSelectedGroupIndex,
    List<JoiningChallengeGroupDto> joiningChallengeGroups
) {}
