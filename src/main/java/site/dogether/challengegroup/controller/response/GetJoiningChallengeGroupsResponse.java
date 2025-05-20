package site.dogether.challengegroup.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupDto;

import java.util.List;

public record GetJoiningChallengeGroupsResponse(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    int lastSelectedGroupIndex,
    List<JoiningChallengeGroupDto> joiningChallengeGroups
) {}
